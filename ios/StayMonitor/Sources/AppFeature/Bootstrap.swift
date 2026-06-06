import Dependencies
import SQLiteData
import Schema

public extension DependencyValues {
    mutating func bootstrapDatabase() throws {
        let database = try SQLiteData.defaultDatabase()
        var migrator = DatabaseMigrator()
        #if DEBUG
            migrator.eraseDatabaseOnSchemaChange = true
        #endif
        AppDatabase.registerMigrations(&migrator)
        try migrator.migrate(database)
        defaultDatabase = database
    }
}
