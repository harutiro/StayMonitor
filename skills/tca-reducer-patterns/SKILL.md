---
name: tca-reducer-patterns
description: "StayMonitorプロジェクト固有のTCA Reducer実装パターンガイド。Reducer基本構造、ViewAction/InternalAction/DelegateAction設計、DataStateパターン、@FetchOne/@FetchAll、子Reducer統合（Scope）、Effectパターン（.run制約、Result）、エラーハンドリング、Computed Propertyを提供。Use when (1) 新しいTCA Reducerを作成する時、(2) Action設計パターンを確認したい時、(3) DataStateによるロード状態管理を実装する時、(4) 子Reducerの統合方法を知りたい時、(5) Effectの書き方を確認したい時。"
---

# TCA Reducer 実装パターン

StayMonitorプロジェクトでTCA Reducerを実装する際のパターン集。
基本ルールは `.claude/rules/TCA_ARCHITECTURE.md` を参照。

## Reducer 基本構造

```swift
@Reducer
public struct XXXReducer: Sendable {
    @ObservableState
    public struct State: Sendable, Equatable {
        var dataState: DataState<[Item], EquatableError> = .idle
        public init() {}
    }

    public enum Action: Sendable, ViewAction {
        case view(ViewAction)
        case `internal`(InternalAction)

        @CasePathable
        public enum ViewAction: Sendable {
            case onAppear
        }

        @CasePathable
        public enum InternalAction: Sendable {
            case fetchResponse(Result<[Item], any Error>)
        }
    }

    @Dependency(\.xxxUseCase) var xxxUseCase
    public init() {}

    public var body: some ReducerOf<Self> {
        Reduce { state, action in
            switch action {
            case .view(.onAppear):
                guard case .idle = state.dataState else { return .none }
                state.dataState.startLoading()
                return .run { send in
                    await send(.internal(.fetchResponse(Result {
                        try await xxxUseCase.fetch()
                    })))
                }

            case let .internal(.fetchResponse(.success(items))):
                state.dataState.setSuccess(items)
                return .none

            case let .internal(.fetchResponse(.failure(error))):
                state.dataState.setFailure(EquatableError(error))
                return .none
            }
        }
    }
}
```

## 最重要禁止ルール

**Actionを関数代わりに使用しない:**
```swift
// NG
return .send(.internal(.updateState))
// OK
state.value = newValue
return .none
```

**子Reducerへは reduce(into:action:) を使用:**
```swift
// NG
return .send(.driveRecord(.view(.urlCommandStart)))
// OK
return reduce(into: &state, action: .driveRecord(.view(.urlCommandStart)))
```

**.run 内は1つのClient/UseCase呼び出しのみ**

## 詳細パターン

Action設計、DataState、@FetchOne/@FetchAll、子Reducer統合、エラーハンドリング、Computed Propertyの詳細:

→ [references/patterns.md](references/patterns.md)
