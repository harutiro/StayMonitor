# StayMonitor

滞在管理アプリ。地図（OpenStreetMap）上のピンを中心に半径100mの Geofence を作り、入室(ENTER)/退出(EXIT)を自動記録して滞在履歴を見せる。
**Android と iOS のモノレポ。**

## どこに何があるか

| パス | 内容 |
| --- | --- |
| リポジトリ直下（`app/`, `build.gradle.kts`, `gradlew` 等） | **Android** アプリ。詳細は `README.md` |
| `ios/ios-stay-monitor.xcodeproj` | **iOS** アプリのプロジェクト（ターゲットは薄い） |
| `ios/StayMonitor/` | **iOS のコード本体**（ローカル Swift Package。`Package.swift` で依存/モジュール管理） |
| `.claude/rules/` | 開発ルール（TCA / ベストプラクティス） |
| `agents/`, `skills/` | Claude 用 agents / skills の正本（`.claude/agents`・`.claude/skills` から symlink） |
| `.claude/hooks/`, `.claude/settings.json` | 静的解析フック設定 |
| `.mcp.json` | MCP 設定（XcodeBuildMCP） |

`CLAUDE.md` が正本、`AGENTS.md` はその symlink。

## iOS アーキテクチャ

- **SwiftUI + The Composable Architecture (TCA) + swift-dependencies**、Swift 6 言語モード（Xcode 26 / iOS 26）。
- アプリターゲット `ios-stay-monitor` は薄く保つ: `@main StayMonitorApp` がストアを生成し `AppView(store:)` に渡すだけ。
- ロジック・画面は `ios/StayMonitor/` パッケージに書く。**1パッケージ内マルチモジュール**: `Sources/<Module>/` ＋ `Tests/<Module>Tests/`、`Package.swift` の `.target` を増やして分割（workspace や複数パッケージにはしない）。
  - 現状モジュールは `AppFeature`（ルート Feature + View）のみ。今後 `StayMonitorCore`（entity/interface）等を追加。
- TCA の作法 → `.claude/rules/TCA_ARCHITECTURE.md` / 実装パターン → `skills/`（tca-reducer-patterns ほか）。
- **PointFree 系ライブラリ（TCA/Dependencies/Sharing/SQLiteData 等）の使い方は各自インストール済みの `pfw-*` ユーザースキルに従う**。個人ライセンスのため repo に vendor しない。

## ハーネス（コーディング時に効く設定）

- **ルール**: `.claude/rules/TCA_ARCHITECTURE.md`, `.claude/rules/BEST_PRACTICES.md`
- **Sub Agents**（`agents/`）: `swiftui-component-designer` / `tca-feature-builder` / `tca-test-writer` / `tca-test-reviewer` / `client-pattern-expert` / `usecase-implementer` / `swift-testing-expert`
- **静的解析フック**: `ios/` 配下の `.swift` 編集時に `.claude/hooks/post-edit-swift.sh` が自動実行 → `swift-format`(`ios/.swift-format`) で整形 ＋ `SwiftLint`(`--strict`, `ios/.swiftlint.yml`)。フォーマット系ルールは `disabled_rules` で swift-format に委譲。
- **ビルド/シミュレータ**: XcodeBuildMCP（`.mcp.json`）。scheme = `ios-stay-monitor`。

## Android

スタック等は `README.md` 参照。lint: `./gradlew ktlintCheck` / `ktlintFormat`（= `make ktlint-check` / `ktlint-format`）。
