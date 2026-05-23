# 開発ベストプラクティス

このドキュメントはプロジェクト全体で適用される開発ベストプラクティスを定義します。

## コーディング原則

### DRY原則
- 同じコードを繰り返し書かない
- 共通のロジックは適切に抽出する
- ただし、過度な抽象化は避ける

### Swiftの型システム活用
- Errorなどの存在型は必ず`any`をつける（例: `any Error`）
- 型安全性を最大限活用する

## アクセスレベル（Access Control）

### デフォルトはinternal
- **publicにするのはモジュール外から使う場合のみ**。迷ったらinternalにしろ
- テストからは `@testable import` を使えばinternal以上にアクセスできる。テストのためだけにpublicにするな
- `open` は継承が必要な場合のみ。ほぼ使わない

### 判断基準
| 状況 | アクセスレベル |
|------|-------------|
| 同一モジュール内でのみ使用 | `internal`（デフォルト、明示不要） |
| 他モジュールから参照される | `public` |
| テストからのみアクセスしたい | `internal` + `@testable import` |
| サブクラス化を許可する | `open`（要ユーザー確認） |
| ファイル内でのみ使用 | `private` |
| 同一ファイルのextensionからも使う | `fileprivate` |

## プレビュー・モック関連

### 禁止事項
- **mockを書くな**: テスト用のモックデータは基本的に不要
- **previewValueは不要**: SwiftUIのPreview用の値は基本的に作らない
- **previewは基本いらない**: 必要最小限に留める

### 理由
- 実装とモックのメンテナンスコストが二重にかかる
- 実際のデータフローとの乖離が生じる
- テストは実際のデータフローで行うべき

## 変更管理

### 既存実装の尊重
- **動いている実装を無闇に動かすな**
- 既存のコードが正しく動作している場合、リファクタリングは慎重に
- 変更する場合は明確な理由と改善点を持つこと

## 開発効率化

### エージェントの活用
- **agentsを頻繁に利用しろ**
- TCA、SwiftUI、テストなど、各分野の専門エージェントを積極的に使う
- 参照: `CLAUDE.md` の Sub Agents セクション

### 並列実行
- **taskはなるべく並列で行え**
- 独立したタスクは並列実行して効率化
- 依存関係のないツール呼び出しは同時に実行

## 並行処理（Concurrency）

### Actorを使え
- **NSLock、DispatchQueue.sync、その他のロック機構は使用禁止**
- スレッドセーフな状態管理には必ず`actor`を使用する
- Swift Concurrencyの機能を最大限活用する

```swift
// ✅ OK - actorを使用
private actor Buffer {
    private var items: [Item] = []

    func append(_ item: Item) {
        items.append(item)
    }

    func drain() -> [Item] {
        let result = items
        items = []
        return result
    }
}

// ❌ NG - NSLockを使用
private let lock = NSLock()
private var items: [Item] = []

func append(_ item: Item) {
    lock.lock()
    items.append(item)
    lock.unlock()
}
```

**理由:**
- actorはSwift Concurrencyの一部として設計され、データ競合を防ぐ
- コンパイラによる安全性検証が得られる
- async/awaitと自然に統合される
- NSLockなどは手動管理が必要でデッドロックのリスクがある

## 関数分割時のコメントルール

`function_body_length` や `cyclomatic_complexity` の違反で関数を分割した場合、**caller（呼び出し元）とcallee（呼び出し先）の両方にコメントを必ずつけろ。**

### callee（分割先の関数）に必須
- **doc comment**: 何をする関数か、引数と返り値の意味
- 複雑なロジックには**インラインコメント**で処理の意図を説明

### caller（呼び出し元）に必須
- **なぜこの関数を呼んでいるか**をインラインコメントで説明

```swift
// ✅ 正しい例
/// 一時停止期間を除外した総走行距離を計算する
/// - Parameters:
///   - locationPoints: 時系列順のLocationPoint配列
///   - pausePeriods: 除外する一時停止期間
/// - Returns: 総走行距離（メートル）
static func calculateTotalDistance(
    locationPoints: [LocationPoint],
    pausePeriods: [PausePeriod],
) -> Double { ... }

// caller側
// 一時停止を除外した実走行距離を算出
let distance = DistanceCalc.calculateTotalDistance(
    locationPoints: points,
    pausePeriods: pauses,
)
```

### このルールが適用される場面
- SwiftLint違反で関数を分割した時
- 複雑なロジックをヘルパー関数として切り出した時
- テスト可能にするためにprivate関数をinternal/publicに昇格した時

## ❌ 禁止事項

### swiftlint:disable の使用制限

**`// swiftlint:disable` コメントはユーザーの明示的な許可なく追加してはならない。**

- `swiftlint:disable:next`、`swiftlint:disable:this`、`swiftlint:disable` / `swiftlint:enable` ブロック、いずれも対象
- lint違反はコードの修正で解決すること。以下の修正ガイドに従え：

| SwiftLint違反 | 修正方法 |
|--------------|---------|
| `large_tuple` | タプルを `private struct` に置換せよ |
| `function_body_length` | ヘルパー関数に分割せよ。分割後は caller/callee 両方にdoc commentとインラインコメントを必ずつけろ（下記参照） |
| `cyclomatic_complexity` | 条件分岐をメソッド抽出やearly returnで分割せよ。分割後は caller/callee 両方にdoc commentとインラインコメントを必ずつけろ（下記参照） |
| `function_parameter_count` | パラメータを構造体にまとめよ |
| `type_body_length` | ファイル分割（`+Extension.swift`）で対応せよ |
| `file_length` | モジュール分割またはファイル分割で対応せよ |
| `multiline_arguments` | 各引数を1行ずつに分けよ |
| `force_unwrapping` | `guard let` / `if let` に書き換えよ（ただしURL/Dateリテラル等の安全な使用はwarningのまま残してよい） |

- どうしてもdisableが必要な場合は、まずユーザーに理由を説明して許可を得ること
- `.swiftlint.yml` の `disabled_rules` への追加も同様にユーザーの許可が必要

### キャッシュフォルダの削除禁止

**以下のフォルダを安易に削除してはならない:**
- `DerivedData` (~/Library/Developer/Xcode/DerivedData/)
- `.build` フォルダ

**理由と対処法:**
- ビルドエラーが発生しても、まずエラーメッセージを正確に読み、原因を特定すること
- キャッシュ削除は最終手段であり、ユーザーの明示的な許可なく実行してはならない
- キャッシュを削除すると次回ビルドに大幅に時間がかかる

## エージェント一覧

以下のエージェントが利用可能です：

### UI/コンポーネント系
- `swiftui-component-designer` - SwiftUIのComponentを設計する際

### TCA系
- `tca-feature-builder` - TCAのFeatureを設計する際
- `tca-test-writer` - TCAのReducerのテストコードを書く
- `tca-test-reviewer` - TCAのReducerのテストコードをレビューする

### アーキテクチャ系
- `client-pattern-expert` - DBやAPIのClientを設計する際
- `usecase-implementer` - 複数Clientを用いたlogicを実装する際

## Gotchas

### SwiftLint × SwiftFormat の競合
- フォーマット系ルール（`trailing_comma`, `opening_brace`, `colon`, `void_return` 等）は `.swiftlint.yml` の `disabled_rules` でSwiftFormatに委ねている。SwiftLint側でこれらのルールを有効化しようとするな
- post-editフックは `--strict` で実行されるため、`severity: warning` のルールも実質ブロックになる。warningだから通ると思うな

### TCA Reducer の `return .send(` / `reduce(into:action:)`
- 自分のActionへの `.send()` も `reduce(into:action:)` も禁止。共通処理は**関数として抽出**せよ
- 親Reducerから子Reducer（Scope管理）への `reduce(into:action:)` は正当な用途。これはOK
- `.send(.delegate())` も正当。子→親への委譲通知

### custom_rules のエラーメッセージ
- custom_rulesのmessageに修正方法と `.claude/rules/` へのリンクを埋め込んでいる。AIエージェントはエラーメッセージをコンテキストとして読み込むため、メッセージ自体が修正指示書として機能する

### post-edit-swift.sh (Claude Code PostToolUse hook)
- `ios/` 配下の `.swift` をEdit/Write時に自動実行される。SwiftFormatで整形し、SwiftLint(`--strict`)違反があれば差し戻す
- CLAUDE.mdのルールは「提案」（コンテキストウィンドウから押し出される可能性あり）だが、hookは「保証された実行」

## 参考資料

- TCAの詳細: `.claude/rules/TCA_ARCHITECTURE.md`
- 実装パターン: `skills/`（tca-reducer-patterns / client-implementation-patterns / navigation-and-flow-patterns）
