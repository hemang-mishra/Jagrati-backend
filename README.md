<img width="114" height="115" alt="JagratiAppLogo" src="https://github.com/user-attachments/assets/a472b331-ce73-4446-9f0a-ebfea0bfb012" />

# Jagrati Backend Server

## Introduction

This is the backend server for the Jagrati Android app, a comprehensive management system for the Jagrati initiative at IIITDM Jabalpur. Jagrati is a non-profit student initiative dedicated to educational and social welfare, focusing on providing quality education to underprivileged children in the surrounding areas of the college.

The backend provides RESTful APIs to support all the features of the Jagrati mobile application, including user management, attendance tracking, academic progress monitoring, and volunteer coordination.

## Features

The backend server supports nine comprehensive feature modules:

### 1. **User Management & Role System**
- JWT-based authentication and authorization
- Role-based access control with granular permissions
- User registration and profile management
- Volunteer request processing with approval workflows
- Role transition management (upgrade/downgrade requests)
- OAuth2 integration for social login

### 2. **Student & Volunteer Profiles**
- Complete student and volunteer profile management
- Village and group organization system
- Facial recognition data storage for attendance
- Student group assignment history tracking
- Profile image management and storage

### 3. **Attendance Management**
- Student attendance tracking with facial recognition support
- Volunteer attendance management with role-based marking
- Bulk attendance operations
- Attendance analytics and reporting
- Date-wise and student-wise attendance queries

### 4. **Syllabus Management**
- Subject, topic, and subtopic hierarchy management
- Class-wise curriculum organization (Classes 1-12)
- Educational resource sharing by volunteers
- Content management with text and link resources
- Academic content versioning and updates

### 5. **Volunteer Scheduling**
- Day-wise volunteer schedule management
- Group and subject assignment APIs
- Schedule modification request handling
- Conflict detection and resolution
- Automated scheduling optimization

### 6. **Classwork & Homework**
- Class session management with time tracking
- Homework assignment and submission tracking
- Student progress scoring and feedback system
- Image upload support for classwork documentation
- Session-based topic coverage tracking

### 7. **Student Progress & Remarks**
- Individual student proficiency tracking APIs
- Topic-wise progress monitoring and analytics
- Volunteer remarks and observation system
- Community-based remark validation with voting
- Performance analytics and reporting

### 8. **Volunteer Ranking & Activities**
- Point-based volunteer ranking system
- Activity logging and recognition APIs
- Performance tracking across engagement types
- Leaderboard generation and maintenance
- Achievement and milestone tracking

### 9. **Posts & Social Features**
- Community post management
- Image upload and sharing capabilities
- Like and engagement tracking
- Category-based content organization
- Social interaction analytics

## Tech Stack

- **Spring Boot 3.x** – Main application framework
- **Kotlin** – Primary programming language
- **PostgreSQL** – Primary database
- **Spring Security** – Authentication and authorization
- **JWT (JSON Web Tokens)** – Stateless authentication
- **Spring Data JPA** – Database ORM
- **OAuth2** – Social authentication integration
- **Spring Mail** – Email functionality
- **Thymeleaf** – Template engine for emails
- **SpringDoc OpenAPI** – API documentation
- **Google API Client** – Google services integration

## Database Schema

The complete database schema with all tables, relationships, and indexes is available here:
[📋 **View Complete Database Schema**](https://dbdiagram.io/d/Jagrati-DB-68820793cca18e685c88b1c1)

The schema includes:
- 30+ tables organized across 9 feature modules
- Comprehensive foreign key relationships
- Optimized indexes for performance
- Detailed field specifications and constraints

## API Documentation

Once the server is running, you can access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Getting Started

### Prerequisites
- **Java 17** or later
- **PostgreSQL 13** or later
- **Gradle 8.x** (wrapper included)
- **Google OAuth2 credentials** (for social login)
- **SMTP server** (for email functionality)

### Environment Variables

Before running the server, set up the following environment variables:

```bash
# JWT Configuration
JWT_SECRET_BASE_64=your_base64_encoded_jwt_secret

# Email Configuration
GMAIL_PASS=your_gmail_app_password

# OAuth2 Configuration
OAUTH2_CLIENT_ID=your_google_oauth_client_id
OAUTH2_SECRET=your_google_oauth_secret
```

### Installation & Setup

1. **Clone the repository**
```bash
git clone https://github.com/hemang-mishra/Jagrati-backend.git
cd Jagrati-backend
```

2. **Set up PostgreSQL Database**
```sql
-- Create database
CREATE DATABASE jagrati_db;

-- Create user (optional)
CREATE USER jagrati_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE jagrati_db TO jagrati_user;
```

3. **Configure Environment Variables**
```bash
# Option 1: Export in terminal
export JWT_SECRET_BASE_64="your_jwt_secret"
export DB_URL="jdbc:postgresql://localhost:5432/jagrati_db"
# ... other variables

# Option 2: Create .env file (if supported)
# Option 3: Set in IDE run configuration
```

4. **Build and Run**
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

5. **Verify Installation**
- Server should start on `http://localhost:8080`
- Check health endpoint: `GET /actuator/health`
- Access API docs: `http://localhost:8080/swagger-ui.html`

### Database Migration

The application uses Spring Boot's automatic schema generation. On first run:
- Tables will be created automatically based on JPA entities
- Initial data seeding may be required for roles and permissions
- Check application logs for any migration issues


## Architecture

The server follows modern Spring Boot practices:

- **Layered Architecture**: Controller → Service → Repository
- **RESTful API Design**: Resource-based URLs with proper HTTP methods
- **Security**: JWT-based stateless authentication
- **Database**: JPA with PostgreSQL for data persistence
- **Documentation**: Auto-generated OpenAPI specifications
- **Configuration**: Environment-based configuration management


## Contributing Guidelines

We welcome contributions from the community! Please follow these guidelines:

### How to Contribute

1. **Fork the repository** and create your feature branch from `master`
2. **Create a new branch**: `git checkout -b feature/your-feature-name`
3. **Make your changes** following the coding standards
4. **Test your changes** thoroughly
5. **Commit your changes** with descriptive messages
6. **Push to your fork** and create a Pull Request

### Coding Standards

- Follow **Kotlin coding conventions**
- Use **Spring Boot best practices**
- Write **meaningful commit messages**
- Add **proper error handling**
- Include **unit and integration tests**
- Document **complex business logic**
- Follow **RESTful API design principles**

### Development Setup

1. **IDE Setup**: IntelliJ IDEA recommended
2. **Code Style**: Follow Kotlin official style guide
3. **Testing**: Write tests for new features
4. **Documentation**: Update API docs for changes

### Pull Request Process

1. Ensure all tests pass
2. Update documentation if needed
3. Follow the existing code structure
4. Reference related issues in PR description
5. Request review from maintainers

### Database Changes

- Create migration scripts for schema changes
- Test migrations thoroughly
- Document breaking changes
- Consider backward compatibility

## Deployment

### Production Considerations

- Use production-grade PostgreSQL instance
- Set up proper logging and monitoring
- Configure HTTPS/SSL certificates
- Set up database connection pooling
- Configure proper CORS settings
- Use production JWT secrets
- Set up backup strategies


## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check PostgreSQL is running
   - Verify connection string and credentials
   - Ensure database exists

2. **JWT Token Issues**
   - Verify JWT_SECRET_BASE_64 is properly encoded
   - Check token expiration settings

3. **OAuth2 Setup**
   - Verify Google OAuth credentials
   - Check redirect URLs configuration

4. **Email Configuration**
   - Ensure Gmail app password is correct
   - Check SMTP settings

## License

This project is developed for educational and social welfare purposes as part of the Jagrati initiative at IIITDM Jabalpur.

## Support

For technical issues and questions:
- Create an issue in the GitHub repository
- Contact the development team
- Check the documentation and API specs

---

**Built with ❤️ by [Hemang Mishra](https://github.com/hemang-mishra)**

For the Android app that consumes these APIs, visit: [Jagrati Android App Repository](https://github.com/hemang-mishra/Jagrati-Android)
