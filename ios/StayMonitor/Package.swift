// swift-tools-version: 6.2
import PackageDescription

let package = Package(
    name: "StayMonitor",
    platforms: [
        .iOS(.v26)
    ],
    products: [
        .library(name: "AppFeature", targets: ["AppFeature"])
    ],
    dependencies: [
        .package(url: "https://github.com/pointfreeco/swift-composable-architecture", from: "1.22.0"),
        .package(url: "https://github.com/pointfreeco/swift-dependencies", from: "1.9.0"),
    ],
    targets: [
        .target(
            name: "AppFeature",
            dependencies: [
                .product(name: "ComposableArchitecture", package: "swift-composable-architecture"),
                .product(name: "Dependencies", package: "swift-dependencies"),
            ]
        ),
        .testTarget(
            name: "AppFeatureTests",
            dependencies: ["AppFeature"]
        ),
    ],
    swiftLanguageModes: [.v6]
)
