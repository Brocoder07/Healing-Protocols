Healing Protocols

This is a mobile application designed for users to search and access information about acupuncture patterns, symptoms, and treatment points. The app is built using Jetpack Compose for Android, is intended for interns, students, and practitioners for a certain clinic, led by a licensed acupuncture professional. The app allows users to search through medical data provided by the clinic.

Features
Search Functionality: Users can search for acupuncture organs, patterns, symptoms, and corresponding treatment points.
Offline Access: Cached data ensures that users can access information without an internet connection incase API calls take time to load the response to the frontend.
User Authentication: Only authorized individuals (interns, students, and practitioners) with custom email domains can sign in.
Navigation: Simple navigation between the home page, search results, and documentation.
Admin Content: All medical data provided in the app has been approved by the clinic for which the app was developed, ensuring its accuracy and relevance.

Technologies Used
Jetpack Compose: For modern UI development.
FastAPI: Backend for data handling and API endpoints.
Firebase Authentication: To manage user login and sign-up.
MongoDB Atlas: For storing clinical data.
Android Studio: Development environment for the app.
