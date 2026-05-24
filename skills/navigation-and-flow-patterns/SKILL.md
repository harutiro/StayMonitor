---
name: navigation-and-flow-patterns
description: "StayMonitorプロジェクト固有の画面遷移・ナビゲーション・UXフローパターンガイド。@Reducer enumによるルートレベル画面切替、TabViewナビゲーション、@Presents + Destination enumによるPush/Sheet/FullScreenCover遷移、AlertStateパターン、Settings固有のViewModifier分離パターン、DeepLink処理パターン、reduce(into:action:)による親子Action転送を提供。Use when (1) 新しい画面遷移を実装する時、(2) Navigation/Sheet/Alert のTCAパターンを確認したい時、(3) DeepLink処理を実装する時、(4) 親Reducerから子Reducerにアクションを送る方法を知りたい時。"
---

# 画面遷移・ナビゲーション・UXフローパターン

StayMonitorプロジェクトでの画面遷移パターン集。
基本ルールは `.claude/rules/TCA_ARCHITECTURE.md` を参照。

## アプリ全体の画面フロー

```text
RootView (RootReducer)
  ├─ LaunchScreen
  ├─ Onboarding
  └─ MainApp (AppReducer) ── TabView
       ├─ Histories (DriveHistoryReducer)
       ├─ Record (DriveRecordReducer)
       ├─ Analytics (DriveAnalyticsReducer)
       └─ Settings (SettingsReducer)
```

## パターン使い分けガイド

| ユースケース | パターン |
|---|---|
| 排他的画面切替 | `@Reducer enum` (Transition) |
| タブ切替 | `TabView` + `BindableAction` |
| Push遷移（少数） | `Destination` enum + `.navigationDestination(item:)` |
| Push遷移（多数） | 個別 `@Presents` + `.navigationDestination(item:)` |
| Sheet | `@Presents` + `.sheet(item:)` |
| FullScreenCover | `@Presents` + `.fullScreenCover(item:)` |
| Alert | `@Presents AlertState` + `.alert()` |
| DeepLink | キューイング + `reduce(into:action:)` |

## Push遷移の基本形（Destination enum）

**Reducer側:**
```swift
@Presents var destination: Destination.State?

@Reducer
public enum Destination {
    case detail(DetailReducer)
    case note(NoteReducer)
}

// 遷移トリガー
state.destination = .detail(DetailReducer.State(session: session))

// body
.ifLet(\.$destination, action: \.destination)
```

**View側:**
```swift
.navigationDestination(
    item: $store.scope(state: \.destination?.detail, action: \.destination.detail)
) { store in
    DetailView(store: store)
}
```

## 詳細パターン

@Reducer enum、TabView、個別@Presents、Alert/ConfirmationDialog、ViewModifier分離、DeepLink処理の詳細:

→ [references/patterns.md](references/patterns.md)
