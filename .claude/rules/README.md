# StayMonitor 開発ルール集

このディレクトリには、StayMonitor (iOS) プロジェクトの開発ルールとベストプラクティスが含まれています。

## ルールファイル一覧

### [TCA_ARCHITECTURE.md](./TCA_ARCHITECTURE.md)
The Composable Architecture (TCA) に関するプロジェクト固有のルール

**内容:**
- .run {} 内の制約
- Client実装ルール
- データ変換ルール
- Actionの使い方（最重要: Actionを関数代わりに使用しない）
- 親Reducerから子Reducerへのアクション送信

**こんな時に参照:**
- TCAのFeatureを実装する時
- ClientやUseCaseを作成する時
- Reducerのアクションを設計する時

### [BEST_PRACTICES.md](./BEST_PRACTICES.md)
プロジェクト全体の開発ベストプラクティス

**内容:**
- コーディング原則（DRY原則、型システム活用）
- アクセスレベル（Access Control）の判断基準
- プレビュー・モック関連の禁止事項
- 変更管理（既存実装の尊重）
- 並行処理（actorを使う / ロック機構の禁止）
- swiftlint:disable の使用制限と違反修正ガイド
- キャッシュフォルダ削除の禁止

**こんな時に参照:**
- 新しく参加したメンバーがプロジェクトの方針を理解する時
- エージェントを使いたい時
- SwiftLint違反の修正方法を知りたい時

## 使い方

### 新規参加メンバー向け
1. まず `BEST_PRACTICES.md` を読んでプロジェクト全体の方針を理解する
2. `TCA_ARCHITECTURE.md` でTCAの実装ルールを確認する

### 実装中
- TCA実装中 → `TCA_ARCHITECTURE.md`
- SwiftUI / Reducer / Client のパターン → `skills/`（tca-reducer-patterns, client-implementation-patterns, navigation-and-flow-patterns）
- PointFreeライブラリ（TCA/Dependencies等）の使い方 → 各自インストール済みの `pfw-*` ユーザースキル

### ルールがわからない時
1. まずこの README.md を読む
2. 該当するルールファイルを確認する
3. それでもわからなければ、Claudeに `.claude/rules/<filename>.md を参照して` と依頼する

## コンテキスト管理

これらのルールファイルは、CLAUDE.mdを簡潔に保ちながら、必要な時に必要なルールだけを参照できるようにするために分割されています。

## メンテナンス

- ルールの追加や変更があった場合は、該当するファイルを更新してください
- 新しいカテゴリのルールが必要な場合は、新しいファイルを作成し、この README を更新してください
- すべてのルールファイルは日本語で記述してください
