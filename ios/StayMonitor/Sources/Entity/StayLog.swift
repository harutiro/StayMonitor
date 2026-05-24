import Foundation
import SQLiteData
import StructuredQueries

/// 滞在ログ。Geofence への入室(ENTER)で生成し、退出(EXIT)で `exitedAt` を埋める。
///
/// `pinID` には外部キー制約を張らない。Android 版と同様、ピンを削除しても滞在履歴は残す。
/// 表示用に `pinName` を非正規化して保持する。
@Table
public struct StayLog: Identifiable, Hashable, Sendable {
    public let id: UUID
    public var pinID: GeofencePin.ID
    public var pinName: String
    public var latitude: Double
    public var longitude: Double
    public var enteredAt: Date
    public var exitedAt: Date?

    public var isStaying: Bool {
        exitedAt == nil
    }

    public var stayDuration: TimeInterval? {
        guard let exitedAt else { return nil }
        return exitedAt.timeIntervalSince(enteredAt)
    }

    public init(
        id: UUID,
        pinID: GeofencePin.ID,
        pinName: String,
        latitude: Double,
        longitude: Double,
        enteredAt: Date,
        exitedAt: Date? = nil
    ) {
        self.id = id
        self.pinID = pinID
        self.pinName = pinName
        self.latitude = latitude
        self.longitude = longitude
        self.enteredAt = enteredAt
        self.exitedAt = exitedAt
    }
}
