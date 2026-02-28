import Foundation

enum AppTab: Hashable {
    case read
    case search
    case bookmarks
    case notes
    case present
}

@Observable
final class NavigationCoordinator {
    var selectedTab: AppTab = .read
    var pendingNavigation: BibleLocation?

    func navigateToReader(location: BibleLocation) {
        pendingNavigation = location
        selectedTab = .read
    }
}
