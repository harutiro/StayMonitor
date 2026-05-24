# TCA アーキテクチャルール

このドキュメントは The Composable Architecture (TCA) を使用する際のプロジェクト固有のルールを定義します。

## .run {} 内の制約

- .run {}内には一つのusecaseまたはclientの呼び出しのみを実装する
- 複数の処理を行う場合は分割して別々のActionにする

```swift
// ✅ 正しい例
.run { send in
    await send(
        .hogeResponse(Result {
           try await hoge()
        })
    )
}

// ❌ 間違った例 - 複数の処理が混在
.run { send in
    await analyticsClient.logEvent(.appLaunched)
    await authenticationClient.signInAnonymously()
    // 複数のClient呼び出しは禁止
}
```

## Client実装ルール

- ClientインターフェースにはFirebase系やその他の外部ライブラリ依存を含めてはならない
- Clientは純粋なInterfaceとして定義する
- 外部ライブラリの型が返り値に含まれる場合は、独自のstructにmapする

```swift
// ✅ 正しい例 - Client interface
@DependencyClient
public struct AuthenticationClient {
    public var signInAnonymously: () async throws -> User // 独自のUser型
}

// ❌ 間違った例 - Firebase型に依存
public struct AuthenticationClient {
    public var signInAnonymously: () async throws -> FirebaseAuth.User // Firebase依存は禁止
}
```

## データ変換ルール

- ClientLive実装内で外部ライブラリの型から独自型への変換を行う
- 変換処理はClientLive内に隠蔽する

## ❌ 絶対NG: Actionを関数代わりに使用することの禁止

**最重要ルール: Actionは絶対に関数の代わりとして使用してはならない**

TCAのActionは状態遷移を表現するものであり、単なる処理の呼び出しのために使ってはならない。
Actionを介して間接的に状態を変更するのではなく、必要な状態変更は直接そのAction内で行う。

```swift
// ❌ 絶対NG - Actionを関数代わりに使用
case .someAction:
    return .send(.internal(.updateState))  // Actionを関数のように呼び出している

case .internal(.updateState):
    state.value = newValue  // 別のActionで状態変更
    return .none

// ✅ 正しい例 - 状態変更は直接行う
case .someAction:
    state.value = newValue  // 状態変更は直接このAction内で行う
    return .run { send in
        await someClient.doSomething()
    }
```

**理由:**
1. Actionは状態遷移を表現するものであり、内部的な処理の分割手段ではない
2. デバッグ時にAction履歴が無駄に増えて追跡が困難になる
3. コードの可読性が著しく低下する
4. TCAの設計思想に反する

**正しい実装パターン:**
- 状態変更が必要な場合は、そのActionの中で直接state変更を行う
- 副作用(Effect)を伴う処理は.runで実行し、必要に応じて結果をActionで受け取る
- 状態変更と副作用を組み合わせる場合は、先にstate変更、その後.runを返す

## 親Reducerから子Reducerへのアクション送信

親Reducerから子Reducer（Scopeで管理されているReducer）にアクションを送る際は、`reduce(into:action:)`を使用する。

```swift
// ✅ 正しい例 - 親から子へのアクション送信
case .someAction:
    state.selectedTab = .record
    return reduce(into: &state, action: .driveRecord(.view(.urlCommandStart)))

// ❌ 間違った例 - .send()は使わない
case .someAction:
    state.selectedTab = .record
    return .send(.driveRecord(.view(.urlCommandStart)))  // これだとScopeを通らない
```

**理由:**
- `reduce(into:action:)`は現在のReducerのbodyを再実行するため、Scopeで定義された子Reducerが正しく処理される
- `.send()`は新しいアクションをキューに追加するだけで、即座に子Reducerに到達しない場合がある

## 関連エージェント

TCA関連の実装を行う際は、以下のエージェントを活用してください：

- `tca-feature-builder` - TCAのFeatureを設計する際
- `tca-test-writer` - TCAのReducerのテストコードを書く
- `tca-test-reviewer` - TCAのReducerのテストコードをレビューする
- `client-pattern-expert` - DBやAPIのClientを設計する際
- `usecase-implementer` - 複数Clientを用いたlogicを実装する際
