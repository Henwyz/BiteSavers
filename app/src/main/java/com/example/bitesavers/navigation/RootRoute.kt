sealed class RootRoute(val route: String) {
    data object Login : RootRoute("login_screen")
    data object SignUp : RootRoute("signup_screen")
    data object Terms : RootRoute("terms_screen")
    data object CustomerGraph : RootRoute("customer_graph")
    data object BusinessGraph : RootRoute("business_graph")
    data object RegisterRestaurant : RootRoute("register_restaurant_screen")
    data object PendingApproval : RootRoute("pending_approval_screen")
    data object RejectedApproval : RootRoute("rejected_approval_screen")
}