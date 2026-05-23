# Client/UseCase 詳細パターン

## 目次

- [引数の命名規則](#引数の命名規則)
- [デフォルト値パターン](#デフォルト値パターン)
- [エラー型の定義](#エラー型の定義)
- [DB操作パターン](#db操作パターン)
- [並列処理パターン](#並列処理パターン)
- [ロールバックパターン](#ロールバックパターン)
- [@Shared の利用](#shared-の利用)
- [UseCase パターン](#usecase-パターン)
- [nonisolated メソッドパターン](#nonisolated-メソッドパターン)

## 引数の命名規則

```swift
// 名前付き引数（アンダースコア + 引数名）
public var createSession: @Sendable (_ latitude: Double, _ longitude: Double) async throws -> RecordSession.ID

// 引数なし
public var fetchAllSessions: @Sendable () async throws -> [RecordSession]

// 同期メソッド（throwsなし）
public var getUnitID: @Sendable (AdUnitType) -> String = { _ in "" }
```

## デフォルト値パターン

```swift
public var getUnitID: @Sendable (AdUnitType) -> String = { _ in "" }
public var piyo: @Sendable (_ piyo: Int) -> String = { _ in "default" }
```

## エラー型の定義

```swift
public enum XXXError: Error, Sendable {
    case noData
    case insufficientPermissions
    case databaseError(any Error)
    case networkError(underlying: any Error)
}
```

## DB操作パターン

### 読み取り

```swift
func fetchAll() async throws -> [RecordSession] {
    try await database.read { db in
        try RecordSession
            .where { $0.status == RecordSession.SessionStatus.completed }
            .order { $0.stoppedAt.desc() }
            .fetchAll(db)
    }
}

func fetchOne(sessionID: RecordSession.ID) async throws -> RecordSession? {
    try await database.read { db in
        try RecordSession
            .where { $0.id == sessionID }
            .fetchOne(db)
    }
}
```

### 書き込み（Upsert）

```swift
func create() async throws -> RecordSession.ID {
    let id = try await database.write { db in
        try RecordSession.upsert {
            RecordSession.Draft(startedAt: Date())
        }
        .returning(\.id)
        .fetchOne(db)
    }
    guard let id else { throw DatabaseError(message: "...") }
    return id
}
```

### 更新

```swift
func update(sessionID: RecordSession.ID) async throws {
    try await database.write { db in
        try RecordSession
            .where { $0.id == sessionID }
            .update {
                $0.status = .completed
                $0.stoppedAt = Date()
            }
            .execute(db)
    }
}
```

### 削除

```swift
func delete(sessionID: RecordSession.ID) async throws {
    try await database.write { db in
        try RecordSession
            .where { $0.id == sessionID }
            .delete()
            .execute(db)
    }
}
```

### Raw SQL（#sql マクロ）

```swift
let totalPauseDuration = try await database.read { db in
    try #sql(
        """
        SELECT COALESCE(
            SUM((julianday(resumedAt) - julianday(pausedAt)) * 86400),
            0.0
        ) as totalPause
        FROM \(PausePeriod.self)
        WHERE \(PausePeriod.sessionID) = \(sessionID)
          AND resumedAt IS NOT NULL
        """,
        as: Double.self
    )
    .fetchOne(db)
}
```

## 並列処理パターン

```swift
func completeSession(sessionID: RecordSession.ID) async throws {
    async let locationPointsTask = database.read { db in
        try LocationPoint
            .where { $0.sessionID == sessionID }
            .order { $0.timestamp.asc() }
            .fetchAll(db)
    }
    async let endLocationNameTask = geocoderClient.reverseGeocode(endLatitude, endLongitude)

    let (locationPoints, endLocationName) = try await (locationPointsTask, endLocationNameTask)
}
```

## ロールバックパターン

```swift
func createSession() async throws -> RecordSession.ID {
    let id = try await database.write { ... }

    do {
        try await startTimer(sessionID: id)
    } catch {
        try? await deleteSession(sessionID: id)
        throw error
    }

    await startLiveActivity(sessionID: id)
    return id
}
```

## @Shared の利用

```swift
private actor LiveActor {
    @Shared(.codableAppStorage(.measurementSystem)) var measurementSystem: MeasurementSystem = .systemDefault
    @Shared(.appStorage(.hasShownAlwaysAllowUseLocationAlert)) var hasShownAlwaysAllowUseLocationAlert = false
    @Shared(.inMemory(.userInfo)) var userInformation: UserInformation?

    // 読み取り
    let system = measurementSystem

    // 書き込み
    $hasShownAlwaysAllowUseLocationAlert.withLock { $0 = true }
}
```

## UseCase パターン

### Interface

```swift
@DependencyClient
public struct XXXUseCase: Sendable {
    public var execute: @Sendable (_ input: InputType) async throws -> OutputType
}

extension XXXUseCase: TestDependencyKey {
    public static let testValue: Self = .init()
}
```

### Live（複数Client組み合わせ）

```swift
extension XXXUseCase: @retroactive DependencyKey {
    public static let liveValue: Self = .live()

    static func live() -> Self {
        let actor = LiveActor()
        return Self(execute: { input in try await actor.execute(input: input) })
    }

    private actor LiveActor {
        @Dependency(\.clientA) var clientA
        @Dependency(\.clientB) var clientB
        init() {}

        func execute(input: InputType) async throws -> OutputType {
            let dataA = try await clientA.fetch()
            let dataB = try await clientB.fetch()
            return OutputType(a: dataA, b: dataB)
        }
    }
}
```

## nonisolated メソッドパターン

CPU集約的な計算でactorの排他制御が不要な場合:

```swift
private actor LiveActor {
    private nonisolated func calculateStats(locationPoints: [LocationPoint]) -> (max: Double?, min: Double?) {
        guard !locationPoints.isEmpty else { return (nil, nil) }
        let speeds = locationPoints.map(\.speed)
        return (max: speeds.max(), min: speeds.min())
    }
}
```
