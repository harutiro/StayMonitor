// swift-tools-version: 6.2
import PackageDescription

let package = Package(
    name: "StayMonitor",
    platforms: [
        .iOS(.v26)
    ],
    products: [
        .library(name: "AppFeature", targets: ["AppFeature"]),
        .library(name: "Entity", targets: ["Entity"]),
        .library(name: "Schema", targets: ["Schema"]),
    ],
    dependencies: [
        .package(url: "https://github.com/pointfreeco/swift-composable-architecture", from: "1.22.0"),
        .package(url: "https://github.com/pointfreeco/swift-dependencies", from: "1.9.0"),
        .package(url: "https://github.com/pointfreeco/sqlite-data", from: "1.0.0"),
        .package(url: "https://github.com/pointfreeco/swift-structured-queries", from: "0.1.0"),
    ],
    targets: [
        // データモデル（@Table）
        .target(
            name: "Entity",
            dependencies: [
                .product(name: "SQLiteData", package: "sqlite-data"),
                .product(name: "StructuredQueries", package: "swift-structured-queries"),
            ]
        ),
        // DBスキーマ定義・マイグレーション登録
        .target(
            name: "Schema",
            dependencies: [
                .product(name: "SQLiteData", package: "sqlite-data")
            ]
        ),
        // ルートFeature + View
        .target(
            name: "AppFeature",
            dependencies: [
                "Schema",
                .product(name: "ComposableArchitecture", package: "swift-composable-architecture"),
                .product(name: "Dependencies", package: "swift-dependencies"),
                .product(name: "SQLiteData", package: "sqlite-data"),
            ]
        ),
        .testTarget(
            name: "EntityTests",
            dependencies: ["Entity"]
        ),
        .testTarget(
            name: "AppFeatureTests",
            dependencies: [
                "AppFeature",
                "Entity",
                .product(name: "DependenciesTestSupport", package: "swift-dependencies"),
            ]
        ),
    ],
    swiftLanguageModes: [.v6]
)
