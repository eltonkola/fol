# fol

This is a Kotlin Multiplatform project targeting Android, iOS, Web, Desktop, Server.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* `/server` is for the Ktor server application.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the platform-specific folders here too.




1. User Registration:

   •	Generate ECC Key Pair: Generate an ECDSA key pair (public/private) for each user.
   •	Store Private Key: Securely store the private key on the user’s device.
   •	Share Public Key: Share the public key with contacts securely (e.g., via a secure channel or directory).

2. Starting a Conversation:

   •	Generate AES Session Key: Create a new AES session key for encrypting the conversation.
   •	Encrypt Session Key: Encrypt the session key using the recipient’s public ECDSA key.
   •	Send Encrypted Session Key: Send the encrypted session key to the recipient.

3. Sending Messages:

   •	Encrypt Message: Encrypt the message content using AES-GCM with the session key.
   •	Sign Message: Sign the encrypted message using the sender’s private ECDSA key.
   •	Send Message: Send the encrypted and signed message to the recipient.

4. Receiving Messages:

   •	Decrypt Session Key: Decrypt the session key using the recipient’s private ECDSA key.
   •	Decrypt Message: Decrypt the message using AES-GCM with the session key.
   •	Verify Signature: Verify the message signature using the sender’s public ECDSA key.




Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [GitHub](https://github.com/JetBrains/compose-multiplatform/issues).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.