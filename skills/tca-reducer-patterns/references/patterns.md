# TCA Reducer 詳細パターン

## 目次

- [Action 設計パターン](#action-設計パターン)
- [DataState パターン](#datastate-パターン)
- [@FetchOne / @FetchAll パターン](#fetchone--fetchall-パターン)
- [子Reducer統合](#子reducer統合)
- [Effect パターン](#effect-パターン)
- [エラーハンドリング](#エラーハンドリング)
- [Computed Property](#computed-property)
- [ファイル構成](#ファイル構成)

## Action 設計パターン

### ViewAction + InternalAction（推奨）

```swift
public enum Action: Sendable, ViewAction {
    case view(ViewAction)
    case `internal`(InternalAction)

    @CasePathable
    public enum ViewAction: Sendable {
        case onAppear
        case buttonTapped
        case itemSelected(Item)
    }

    @CasePathable
    public enum InternalAction: Sendable {
        case fetchResponse(Result<[Item], any Error>)
        case deleteResponse(Result<Void, any Error>)
    }
}
```

### ViewAction + BindableAction

```swift
public enum Action: Sendable, ViewAction, BindableAction {
    case view(ViewAction)
    case binding(BindingAction<State>)
}

// body内でBindingReducerを先に配置
public var body: some ReducerOf<Self> {
    BindingReducer()
    Reduce { state, action in ... }
}
```

### DelegateAction（親への通知）

```swift
public enum Action: Sendable, ViewAction {
    case view(ViewAction)
    case delegate(DelegateAction)

    public enum DelegateAction: Sendable {
        case sessionCompleted(RecordSession)
        case dismissed
    }
}
```

## DataState パターン

```swift
@ObservableState
public struct State: Sendable, Equatable {
    var dataState: DataState<[Item], EquatableError> = .idle
}

case .view(.onAppear):
    guard case .idle = state.dataState else { return .none }
    state.dataState.startLoading()
    return .run { ... }

case let .internal(.fetchResponse(.success(items))):
    state.dataState.setSuccess(items)
    return .none

case let .internal(.fetchResponse(.failure(error))):
    state.dataState.setFailure(EquatableError(error))
    return .none
```

## @FetchOne / @FetchAll パターン

sqlite-data の自動取得機能:

```swift
@ObservableState
public struct State: Sendable, Equatable {
    @FetchOne(
        RecordSession.where { $0.status != RecordSession.SessionStatus.completed }
    )
    var currentSession: RecordSession?
}
```

## 子Reducer統合

### Scope パターン

```swift
@ObservableState
public struct State: Sendable, Equatable {
    var childFeature = ChildReducer.State()
}

public enum Action: Sendable, ViewAction {
    case childFeature(ChildReducer.Action)
}

public var body: some ReducerOf<Self> {
    Scope(state: \.childFeature, action: \.childFeature) {
        ChildReducer()
    }
    Reduce { state, action in ... }
}
```

### 親から子への Action 送信

```swift
// reduce(into:action:) を使用（.send ではない）
case .someAction:
    state.selectedTab = .record
    return reduce(into: &state, action: .driveRecord(.view(.urlCommandStart)))
```

## Effect パターン

### .run 内で @Dependency を使用

```swift
case let .view(.recordingCompleted(summary)):
    return .run { send in
        @Dependency(\.recordingCompletionUseCase) var useCase
        let output = try await useCase.execute(.init(summary: summary))
    }
```

### Result パターン

```swift
return .run { send in
    await send(.internal(.fetchResponse(Result {
        try await useCase.fetch()
    })))
}
```

## エラーハンドリング

```swift
@ObservableState
public struct State: Sendable, Equatable {
    var errorDescription: String? = nil
    var failedAction: FailedAction? = nil
}

public enum FailedAction: Sendable, Equatable {
    case stopRecording
}

case let .view(.recordingError(errorDescription, failedAction)):
    state.errorDescription = errorDescription
    state.failedAction = failedAction
    return .none

case .view(.retryFailedAction):
    state.errorDescription = nil
    let failedAction = state.failedAction
    state.failedAction = nil
    switch failedAction {
    case .stopRecording:
        state.shouldRetryStopRecording = true
        return .none
    case .none:
        return .none
    }
```

## Computed Property

```swift
@ObservableState
public struct State: Sendable, Equatable {
    public var currentSession: RecordSession?

    public var isRecording: Bool { currentSession?.status == .active }
    public var isPaused: Bool { currentSession?.status == .paused }
    var canStartRecording: Bool { currentSession == nil && !isWatchRecording }
}
```

## ファイル構成

```text
XXXFeature/
  XXXReducer.swift
  XXXView.swift
  Components/
    SomeComponent.swift
  Resources/
    Localizable.xcstrings
```

ReducerとViewは必ず別ファイルに分離する。
