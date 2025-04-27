# WiFi AP Scanner

## 📖 Overview
**WiFi AP Scanner** is a powerful Android application designed to scan, analyze, and compare WiFi networks across various locations. It provides users with in-depth insights into WiFi signal strength, enables data collection at custom locations, and delivers comprehensive statistical analysis to help optimize network performance.

Whether you're troubleshooting weak connections, comparing networks across spaces, or optimizing office WiFi setups, WiFi AP Scanner gives you the tools you need for data-driven decisions.

---

## 🚀 Features

### 🔍 WiFi Network Scanning
- Discover available WiFi networks nearby in real-time.
- View detailed information such as:
  - SSID (Network Name)
  - Signal strength (RSSI)
  - Signal strength live indicator.
- Visual representation of signal strength for each network.

### 📊 Signal Strength Analysis
- Collect multiple signal strength readings for a selected network at a specific location.
- Perform **statistical analysis** of the readings, including:
  - **Minimum** signal strength
  - **Maximum** signal strength
  - **Average** signal strength
  - **Standard Deviation** for signal variance
- View results through intuitive **line charts** for deeper insight into signal stability.

### 🗺️ Location Management
- Save readings under custom location names.
- Use predefined locations or create new ones.
- Seamlessly manage and organize readings based on where they were taken.

### 🆚 Comparison Tools
- Compare signal strength data across different locations.
- Analyze variations in network performance based on environment and distance.
- Visualize comparison results with an easy-to-understand and interactive UI.

### 📝 Logging and Troubleshooting
- Maintain detailed logs for session activity and signal readings.
- Options to:
  - **View** logs within the app.
  - **Copy** logs for external analysis.
  - **Clear** logs to start fresh when needed.

---

## 🏗️ Project Structure

```plaintext
WIFIAP/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/wifi/wifi_ap/
│   │   │   │   ├── activities/         # All main app activities (UI logic)
│   │   │   │   ├── adapters/            # RecyclerView and list adapters
│   │   │   │   ├── models/              # Data models for WiFi and location information
│   │   │   │   ├── utils/               # Utility classes (helpers, common functions)
│   │   │   │   └── MainActivity.java    # App entry point
│   │   │   ├── res/
│   │   │   │   ├── layout/              # XML layout files (UI design)
│   │   │   │   ├── drawable/            # Icons, images, and vector assets
│   │   │   │   ├── values/              # Resource files (strings, styles, colors)
│   │   │   │   └── font/                # Custom fonts (Roboto)
│   │   │   └── AndroidManifest.xml      # App manifest declaration
│   └── build.gradle                     # App-level Gradle config
└── build.gradle                         # Project-level Gradle config
```

---

## 🛠️ Key Components

### 🗂 Layout Files
- `activity_main.xml` — Home screen with scan controls.
- `activity_results.xml` — Display detailed results after readings.
- `dialog_comparison_results.xml` — Popup dialog showing location comparison results.
- `dialog_readings_result.xml` — Popup dialog summarizing readings.
- `item_comparison_result.xml` — UI layout for a comparison result item.
- `item_wifi_network.xml` — UI layout for listing scanned networks.

### 🧩 User Interface Elements
- **Scan Button**: Quickly start a WiFi network scan.
- **Network List**: Browse and select detected WiFi networks.
- **Signal Strength Indicator**: Real-time RSSI visualization.
- **Statistical Cards**: Display minimum, maximum, average, and standard deviation.
- **Location Manager**: Input and manage location names for readings.
- **Comparison Dialogs**: Analyze WiFi strength variations across locations.
- **Log Viewer**: Scrollable view for detailed activity and error logs.

---

## 📋 Usage Instructions

### 🔰 Basic Workflow

1. **Launch the App**
   - Grant all necessary location permissions (mandatory for WiFi scanning).

2. **Scan for Networks**
   - Tap **"Scan Networks"** to discover available WiFi networks.
   - Select a network you wish to analyze.

3. **Collect Readings**
   - Enter a custom location name or select from saved locations.
   - Tap **"Start Reading"** to begin capturing signal strength data.
   - Wait for readings to complete to get accurate statistics.

4. **Analyze and Visualize**
   - View detailed statistical results.
   - Use the chart to observe signal strength fluctuation over time.

5. **Compare Across Locations**
   - Go to **"Compare Locations"**.
   - Review network performance across different locations with easy-to-read graphs and summaries.

### ⚙️ Advanced Tips

- **Create Custom Locations**: Ideal for testing at multiple offices, floors, or rooms.
- **Manage Logs**: 
  - Use the log viewer for debugging.
  - Copy logs if reporting bugs or sharing findings.
  - Clear logs when starting new sessions.

---

## 🧪 Technical Details

- Target API Level: Android 21 (Lollipop) and above.
- Core Libraries:
  - **WiFiManager API** for network scanning.
  - **RecyclerView** for efficient list rendering.
  - **Material Components** for clean, responsive UI.
  - **Chart Libraries** for dynamic signal visualization.
- Permissions are requested and handled at runtime following best practices.

---

## 🔒 Permissions Required

| Permission | Purpose |
|:-----------|:--------|
| `ACCESS_FINE_LOCATION` | Required for scanning nearby WiFi networks (mandatory as of Android 6.0+) |
| `ACCESS_WIFI_STATE` | Access the current WiFi state and information |
| `CHANGE_WIFI_STATE` | Initiate WiFi scans programmatically |

**Note:** Without granting location permissions, WiFi scanning will not work due to Android security policies.

---

## 🎨 Styling and Design

- **Typography**: Roboto custom font family.
- **Theme**: Modern Material Design-inspired dark and light themes.
- **UI Components**:
  - Card-based statistical displays.
  - Color-coded signal strength indicators (green for strong, red for weak).
  - Smooth transitions and dialogs for results and comparison views.

---

## ⚡ Notes and Recommendations

- For best accuracy, **take multiple readings** at each location.
- Ensure location services are turned **ON** when using the app.
- Comparing at different times of the day may yield different results due to interference and usage patterns.
- Environmental factors such as walls, furniture, and interference from other electronics can impact signal strength.

---

## 📢 About

**WiFi AP Scanner** empowers users to make informed decisions about their WiFi networks by providing comprehensive, user-friendly tools for measurement, visualization, and comparison.

*Analyze. Compare. Optimize.*

---