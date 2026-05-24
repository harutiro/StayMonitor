---
name: client-implementation-patterns
description: "StayMonitorプロジェクト固有のClient/UseCase実装パターンガイド。@DependencyClientによるInterface定義、@retroactive DependencyKeyによるLive実装、LiveActorパターン、sqlite-data + StructuredQueriesによるDB操作、@Sharedの利用パターンを提供。Use when (1) 新しいClientやUseCaseを作成する時、(2) ClientLiveの実装パターンを確認したい時、(3) DB操作（read/write/upsert/update/delete/#sql）のコード例が必要な時、(4) 複数Clientを組み合わせたUseCaseを実装する時。"
---

# Client/UseCase 実装パターン

StayMonitorプロジェクトでClient/UseCaseを実装する際のパターン集。
基本ルールは `.claude/rules/TCA_ARCHITECTURE.md` を参照。

## モジュール配置

| 種類 | パス |
|------|------|
| Client Interface | `ios/StayMonitor/Sources/StayMonitorCore/XXXClient/XXXClient.swift` |
| UseCase Interface | `ios/StayMonitor/Sources/StayMonitorCore/XXXUseCase/XXXUseCase.swift` |
| ClientLive | `ios/StayMonitor/Sources/StayMonitoriOS/XXXClientLive/XXXClientLive.swift` |
| UseCaseLive | `ios/StayMonitor/Sources/StayMonitoriOS/XXXUseCaseLive/XXXUseCaseLive.swift` |

## Client Interface 基本形

```swift
@DependencyClient
public struct XXXClient: Sendable {
    public var methodName: @Sendable (_ param: ParamType) async throws -> ReturnType
}

extension XXXClient: TestDependencyKey {
    public static let testValue: Self = .init()
}

public extension DependencyValues {
    var xxxClient: XXXClient {
        get { self[XXXClient.self] }
        set { self[XXXClient.self] = newValue }
    }
}
```

## ClientLive 基本形

```swift
extension XXXClient: @retroactive DependencyKey {
    public static let liveValue: Self = .live()

    static func live() -> Self {
        let actor = LiveActor()
        return Self(
            methodName: { param in try await actor.methodName(param: param) }
        )
    }

    private actor LiveActor {
        @Dependency(\.defaultDatabase) var database
        init() {}

        func methodName(param: ParamType) async throws -> ReturnType {
            // 実装
        }
    }
}
```

## 禁止事項

- NSLock/DispatchQueue.sync 禁止 → actor を使用
- liveValue クロージャ内にロジックを書かない → LiveActor に委譲
- Client Interface に外部ライブラリの型を含めない → 独自型にmap
- previewValue/mock は不要

## 詳細パターン

DB操作、並列処理、ロールバック、@Shared、UseCase実装、nonisolatedメソッドの詳細:

→ [references/patterns.md](references/patterns.md)
