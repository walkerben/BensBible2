import UIKit
import UserNotifications
import XCTest
@testable import BensBible2

final class NotificationLaunchTests: XCTestCase {
    func testDidFinishLaunchingInstallsNotificationDelegate() {
        let appDelegate = AppDelegate()
        let launched = appDelegate.application(
            UIApplication.shared,
            didFinishLaunchingWithOptions: nil
        )

        XCTAssertTrue(launched)
        XCTAssertTrue(
            UNUserNotificationCenter.current().delegate === appDelegate.notificationHandler,
            "A notification tap that launches the app is delivered only to the delegate installed before didFinishLaunching returns"
        )
    }
}
