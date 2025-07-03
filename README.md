# Web Testing Dashboard

A comprehensive web-based testing dashboard for automated browser testing with Selenium WebDriver. This application provides a user-friendly interface for creating, managing, and executing automated tests with real-time monitoring and detailed reporting.

## Features

### Core Functionality
- **Visual Test Builder**: Create automated tests through an intuitive drag-and-drop interface
- **Multi-Browser Support**: Execute tests on Chrome, Firefox, and Edge browsers
- **Real-time Test Execution**: Monitor test progress with live status updates
- **Comprehensive Reporting**: Generate detailed HTML reports with screenshots and execution metrics
- **Test Data Management**: Create and manage dynamic test data sets with variable substitution
- **Parallel Execution**: Run multiple tests concurrently with configurable concurrency settings

### Dashboard Components
- **Dashboard**: Overview with test statistics and recent activity
- **Test Builder**: Visual interface for creating test scenarios
- **Test Runner**: Execute built tests with real-time status monitoring
- **Test Data**: Manage JSON-based test data with variable preview
- **Reports**: View and manage test execution reports with filtering and pagination
- **Analytics**: Historical test performance metrics and trends
- **Settings**: Configure system parameters like concurrency limits

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.x with Java 17+
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Database**: PostgreSQL with JPA/Hibernate
- **Testing**: Selenium WebDriver 4.x
- **Containerization**: Docker with Docker Compose
- **Authentication**: Spring Security with form-based login

### Key Components
- **Test Service**: Manages test execution lifecycle and WebDriver instances
- **Report Service**: Generates HTML reports and manages report files
- **Data Service**: Handles test data management and variable substitution
- **Selenium Integration**: Custom WebDriver wrapper with action implementations
- **Concurrent Execution**: Thread pool-based parallel test execution

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Git

## For Development Setup 
- Java 17 or higher
- Maven 3.6 or higher

### 🐳 Using Docker (Recommended)
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd WebTestingDashboard
   ```

2. Start the application:
   ```bash
   docker-compose up -d
   ```

3. Access the dashboard at `http://localhost:8080`

### Manual Setup
1. Install PostgreSQL and create database:
   ```sql
   CREATE DATABASE WebTestingDashboardDB;
   CREATE USER admin WITH PASSWORD 'admin';
   GRANT ALL PRIVILEGES ON DATABASE WebTestingDashboardDB TO admin;
   ```

2. Configure application properties:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/WebTestingDashboardDB
   spring.datasource.username=admin
   spring.datasource.password=admin
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Default Credentials
- **Username**: `admin`
- **Password**: `admin`

## 📖 Usage Guide

### Creating Tests
1. Navigate to the **Test Builder** tab
2. Add actions by clicking "Add Action" and selecting the desired action type
3. Configure each action with appropriate parameters (locators, values, etc.)
4. Preview the generated JSON in real-time
5. Select browser type and failure handling options
6. Click "Build Test" to save the test

### Managing Test Data
1. Go to the **Test Data** tab
2. Create new data sets with JSON structure
3. Use variables in tests with `${variable.path}` syntax
4. Preview available variables before saving

### Running Tests
1. Open the **Test Runner** tab
2. View all built tests with their configurations
3. Click "Run Test" to execute individual tests
4. Monitor real-time progress with status indicators
5. Cancel running tests if needed

### Viewing Reports
1. Access the **Reports** tab to view all generated reports
2. Filter and paginate through historical reports
3. Click "View" to open detailed HTML reports
4. Delete old reports to manage storage

## Configuration

### Environment Variables
- `SPRING_PROFILES_ACTIVE`: Set to `docker` for containerized deployment
- `SPRING_DATASOURCE_URL`: Database connection URL
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password

### Application Properties
- `parallel.tests.max`: Maximum number of concurrent test executions
- `dashboard.security.username`: Admin username
- `dashboard.security.password`: Admin password

## 🐳 Docker Deployment

The application includes a complete Docker setup with:
- PostgreSQL database container
- Web application container
- Persistent data volumes
- Health checks and dependency management

### Docker Compose Services
- `postgres`: PostgreSQL 15 database
- `web-testing-dashboard`: Spring Boot application

### Volumes
- `postgres_data`: Database persistence
- `./reports`: Test report files
- `./screenshots`: Test screenshots

## Security Features

- Form-based authentication with Spring Security
- Session management with automatic logout
- CSRF protection for all forms
- Environment-based configuration for credentials
- Secure file handling for reports and screenshots

## Project Structure

```
src/
├── main/
│   ├── java/com/ita07/webTestingDashboard/
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic interfaces
│   │   ├── serviceImpl/         # Service implementations
│   │   ├── model/               # Entity classes
│   │   ├── repository/          # Data access layer
│   │   ├── selenium/            # Selenium WebDriver integration
│   │   ├── config/              # Configuration classes
│   │   └── utils/               # Utility classes
│   └── resources/
│       ├── templates/           # Thymeleaf templates
│       ├── static/              # CSS, JS, images
│       └── application*.properties
```

## Development

### Building the Project
```bash
mvn clean install
```

### Running Tests
```bash
mvn test
```

### Local Development
1. Start PostgreSQL locally or use Docker:
   ```bash
   docker run -d --name postgres -p 5432:5432 -e POSTGRES_DB=WebTestingDashboardDB -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin postgres:15-alpine
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Performance Considerations

- **Concurrent Testing**: Configurable thread pool for parallel test execution
- **Memory Management**: Automatic cleanup of WebDriver instances
- **Database Optimization**: Indexed queries and pagination for large datasets
- **File Management**: Automatic cleanup of old reports and screenshots
- **Resource Pooling**: Efficient WebDriver instance management

## Troubleshooting

### Common Issues
1. **Database Connection**: Ensure PostgreSQL is running and accessible
2. **WebDriver Issues**: Check browser drivers are installed and up-to-date
3. **Port Conflicts**: Ensure ports 8080 and 5432 are available
4. **Permission Issues**: Verify write permissions for reports and screenshots directories

### Logs
Application logs are available in the console output. For Docker deployments:
```bash
docker-compose logs web-testing-dashboard
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Support

For issues, questions, or contributions, please use the project's issue tracker or contact the development team.

---

**Built with ❤️ for automated testing excellence**
