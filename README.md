# I2P Android SAM Bridge

An Android application that provides Simple Anonymous Messaging (SAM) bridge functionality for I2P network connectivity. Enables Android apps to communicate over the I2P network using the SAM protocol.

## Features

- Built-in I2P Router
- SAM Bridge service
- Runs as a foreground service
- Secure session management
- Android 6.0+ support

## Installation

```bash
# Build from source
git clone https://github.com/go-i2p/i2p-android-sam.git
cd i2p-android-sam
./gradlew assembleDebug
```

## Usage

1. Install the app
2. Tap "Start Service" to start the SAM bridge
3. The service runs in the background with a notification
4. Use "Stop Service" to stop the bridge

### For Developers

The SAM bridge listens on `127.0.0.1:7656`. Connect your apps using:

```java
SAMBridge bridge = new SAMBridge(
    "127.0.0.1",  // host
    7656,         // port
    false,        // bool
    properties,   // Properties
    "sam.keys",   // String
    configFile    // File
);
```

## Configuration

- Default host: 127.0.0.1
- Default port: 7656

## Requirements

- Android 6.0 or higher
- Java 8+
- Gradle 6.1.1+

## Contributing

1. Fork the repository
2. Create a feature branch
3. Submit a Pull Request

## License

BSD 3-Clause License

## Security Notes

- Bridge only accepts localhost connections
- Implements secure session management
- Protected UPnP functionality