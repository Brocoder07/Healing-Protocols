Healing Protocols

This is a mobile application designed for users to search and access information about acupuncture patterns, symptoms, and treatment points. The app is built using Jetpack Compose for Android, is intended for interns, students, and practitioners for a certain clinic, led by a licensed acupuncture professional. The app allows users to search through medical data provided by the clinic.

Features
1) Search Functionality: Users can search for acupuncture organs, patterns, symptoms, and corresponding treatment points.
2) Offline Access: Cached data ensures that users can access information without an internet connection incase API calls take time to load the response to the frontend.
3) User Authentication: Only authorized individuals (interns, students, and practitioners) with custom email domains can sign in.
4) Navigation: Simple navigation between the home page, search results, and documentation.
5) Admin Content: All medical data provided in the app has been approved by the clinic for which the app was developed, ensuring its accuracy and relevance.

Technologies Used:
1) Jetpack Compose: For modern UI development.
2) FastAPI: Backend for data handling and API endpoints.
3) Firebase Authentication: To manage user login and sign-up.
4) MongoDB Atlas: For storing clinical data.
5) Android Studio: Development environment for the app.

App Functionality:
 Search: Users can search for acupuncture patterns and treatment points. The app fetches the data from a FastAPI backend, which retrieves information stored in MongoDB Atlas.
 Authentication: Only registered users from the clinic can log in using Firebase Authentication. Users without appropriate domain emails will not be allowed to sign up.
 Documentation: The app provides in-app documentation to help users understand the data and usage of the application.
