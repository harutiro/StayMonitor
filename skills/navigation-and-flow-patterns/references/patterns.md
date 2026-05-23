# 画面遷移 詳細パターン

## 目次

- [@Reducer enum パターン](#reducer-enum-パターン)
- [TabView ナビゲーション](#tabview-ナビゲーション)
- [Destination enum パターン](#destination-enum-パターン)
- [個別 @Presents パターン](#個別-presents-パターン)
- [AlertState パターン](#alertstate-パターン)
- [Settings ViewModifier分離](#settings-viewmodifier分離)
- [DeepLink 処理](#deeplink-処理)
- [親から子へのAction送信](#親から子へのaction送信)

## @Reducer enum パターン

ルートレベルの排他的画面切替に使用。

### Reducer側

```swift
@Reducer
public struct RootReducer: Sendable {
    @ObservableState
    public struct State: Sendable, Equatable {
        var appState: Transition.State = .launchScreen(LaunchScreenReducer.State())
    }

    @Reducer
    public enum Transition {
        case launchScreen(LaunchScreenReducer)
        case mainApp(AppReducer)
        case onboarding(OnboardingReducer)
    }

    public var body: some ReducerOf<Self> {
        Scope(state: \.appState, action: \.appState) {
            Transition.body
        }

        Reduce { state, action in
            switch action {
            case let .internal(.userStateResult(.authenticated(userInfo))):
                state.appState = .mainApp(AppReducer.State(selectedTab: .histories))
                return .none

            case .appState(.onboarding(.onboardingSteps(.delegate(.onboardingCompleted)))):
                state.appState = .mainApp(AppReducer.State())
                return .none
            }
        }
    }
}
```

### View側

```swift
Group {
    let appState = store.scope(state: \.appState, action: \.appState)
    switch appState.case {
    case let .launchScreen(store): LaunchScreenView(store: store)
    case let .mainApp(store): AppView(store: store)
    case let .onboarding(store): OnboardingView(store: store)
    }
}
```

## TabView ナビゲーション

```swift
// Reducer: BindableActionが必要
public enum Action: Sendable, ViewAction, BindableAction {
    case binding(BindingAction<State>)
}

// View
TabView(selection: $store.selectedTab) {
    Tab("Histories", systemImage: "list.bullet", value: .histories) {
        NavigationStack {
            DriveHistoryView(store: store.scope(state: \.driveHistory, action: \.driveHistory))
        }
    }
}
.tabBarMinimizeBehavior(.onScrollDown)
```

- `NavigationStack` はTab内に配置
- プログラマティック切替: `state.selectedTab = .record`

## Destination enum パターン

複数の遷移先を1つのenumで管理。

```swift
// Reducer
@Presents var destination: Destination.State?

@Reducer
public enum Destination {
    case detail(DriveHistoryDetailReducer)
    case allHistoryMap(AllHistoryMapReducer)
    case note(NoteReducer)
}

case let .view(.sessionTapped(session)):
    state.destination = .detail(DriveHistoryDetailReducer.State(session: session))
    return .none

// body
.ifLet(\.$destination, action: \.destination)

// View: Push
.navigationDestination(
    item: $store.scope(state: \.destination?.detail, action: \.destination.detail)
) { store in DetailView(store: store) }

// View: Sheet（同じDestinationからsheet）
.sheet(
    item: $store.scope(state: \.destination?.note, action: \.destination.note)
) { store in NoteView(store: store) }
```

**scope のルール:**
- state: `\.destination?.detail` （オプショナルチェイン）
- action: `\.destination.detail` （オプショナルチェインなし）

## 個別 @Presents パターン

Destinationに含めず個別に管理する場合。

```swift
// Reducer
@Presents var statsSheet: DriveHistoryStatsReducer.State?
@Presents var premiumSheet: PremiumReducer.State?

// body: ifLetにReducerクロージャを渡す
.ifLet(\.$statsSheet, action: \.statsSheet) {
    DriveHistoryStatsReducer()
}

// View
.sheet(item: $store.scope(state: \.statsSheet, action: \.statsSheet)) { store in
    NavigationStack { StatsView(store: store) }
}
```

## AlertState パターン

```swift
// State
@Presents var deleteConfirmationAlert: AlertState<Action.DeleteConfirmationAlertAction>?

// Action
case deleteConfirmationAlert(PresentationAction<DeleteConfirmationAlertAction>)
public enum DeleteConfirmationAlertAction: Sendable {
    case confirmDelete
}

// Alert作成
state.deleteConfirmationAlert = AlertState {
    TextState("Delete".localized(bundle: .module))
} actions: {
    ButtonState(role: .destructive, action: .confirmDelete) {
        TextState("Delete".localized(bundle: .module))
    }
    ButtonState(role: .cancel) {
        TextState("Cancel".localized(bundle: .module))
    }
}

// Neverを使う場合（ボタンアクション不要）
@Presents var saveErrorAlert: AlertState<Never>?
```

## Settings ViewModifier分離

多数のnavigationDestinationがある場合、ViewModifierに分離。

```swift
public var body: some View {
    Form { settingsSections }
        .modifier(SettingsNavigationModifier(store: store))
        .modifier(SettingsSheetsModifier(store: store))
        .modifier(SettingsAlertsModifier(store: store))
}

private struct SettingsNavigationModifier: ViewModifier {
    @Bindable var store: StoreOf<SettingsReducer>

    func body(content: Content) -> some View {
        content
            .navigationDestination(item: $store.scope(state: \.csvExport, action: \.csvExport)) { ... }
            .navigationDestination(isPresented: $store.isShowingLicense) { LicenseView() }
    }
}
```

## DeepLink 処理

```swift
// RootReducer: キューイングまたは即時転送
case let .internal(.deepLinkReceived(deepLink)):
    switch state.appState {
    case .mainApp:
        return reduce(into: &state, action: .appState(.mainApp(.deepLinkReceived(deepLink))))
    case .launchScreen, .onboarding:
        state.queuedDeepLinks.append(deepLink)
        return .none
    }

// AppReducer: タブ切替 + 子Reducerへ転送
case let .deepLinkReceived(deepLink):
    state.selectedTab = .record
    switch deepLink {
    case .startRecording:
        return reduce(into: &state, action: .driveRecord(.view(.urlCommandStart)))
    }
```

## 親から子へのAction送信

```swift
// reduce(into:action:) を使用（Scopeを通過する）
return reduce(into: &state, action: .driveRecord(.view(.urlCommandStart)))

// .send() は使わない（Scopeを通らない可能性がある）
// return .send(.driveRecord(.view(.urlCommandStart)))  // NG
```
