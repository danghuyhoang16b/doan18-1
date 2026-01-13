I will fix the compilation errors by:

1.  **Updating `ApiService.java`**:
    *   Add the missing import: `import retrofit2.http.Query;` to resolve the `cannot find symbol: class Query` error.

2.  **Updating `User.java`**:
    *   Add a `password` field (needed for user creation/updates).
    *   Add setter methods for all fields (`setId`, `setUsername`, `setPassword`, `setFullName`, `setRole`, `setEmail`, `setPhone`, `setAvatar`, `setIsLocked`) to resolve the `cannot find symbol: method setXxx` errors in `AdminUserEditActivity`.

This will allow the app to compile and the "Add/Edit User" functionality to work correctly.