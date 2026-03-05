# Spring Boot GitHub PR Tracker

This tool tracks GitHub Pull Requests (PRs), checks their status, and allows you to add merged PRs to a wiki link. It uses Spring Boot, H2 Database, and a web interface.

## Features
- **Add PR by link**: Input a GitHub PR link to track its status.
- **PR Dashboard**: View all PRs with details: Author, Status, Merged By, Branches, CI details, Conflicts, Out-of-date, Approvals, Change Requests, etc.
- **Sections**: Filter PRs by status (Open, Merged).
- **Wiki Integration**: Mark merged PRs as added to the wiki.
- **H2 Database**: Stores PRs for easy jar execution.
- **Comprehensive PR Details**: 
  - Author name and merged by information
  - Base and compare branches
  - CI/CD pipeline status
  - Merge conflicts detection
  - Branch out-of-date status
  - Number of approvals and change requests

## Prerequisites
- Java 17+
- Maven 3.6+
- GitHub Personal Access Token (PAT) with repo access

## Setup

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd demo
```

### 2. Create GitHub Personal Access Token
1. Go to GitHub → Settings → Developer Settings → Personal Access Tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Give it a descriptive name (e.g., "PR Tracker Tool")
4. Select the following scopes:
   - `repo` (Full control of private repositories)
   - `read:org` (Read org and team membership)
5. Click "Generate token" and **copy the token immediately**

### 3. Configure SSO for Organization Access (Important!)
If your PRs are in an organization with SAML SSO enabled (like `extremenetworks`):
1. After creating the token, you'll see "Configure SSO" next to it
2. Click "Authorize" for your organization(s)
3. Complete the SSO authentication flow
4. **Without this step, you'll get 403 Forbidden errors!**

### 4. Configure the Application
Edit `src/main/resources/application.properties`:
```properties
github.token=YOUR_GITHUB_PAT_HERE
```

Replace `YOUR_GITHUB_PAT_HERE` with your actual token.

### 5. Build the Application
```bash
mvn clean install
```

### 6. Run the Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using the JAR file**
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 7. Access the Dashboard
Open your browser and navigate to:
- **Main Dashboard**: [http://localhost:8080/prs](http://localhost:8080/prs)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:prdb`
  - Username: `sa`
  - Password: *(leave empty)*

## Usage

### Adding a PR
1. Open the dashboard at `http://localhost:8080/prs`
2. Enter a GitHub PR URL in the input field:
   ```
   https://github.com/owner/repo/pull/12345
   ```
3. Click "Add PR"
4. The system will fetch and display PR details automatically

### Viewing PR Details
The dashboard displays:
- **Author**: Who created the PR
- **Status**: Open, Merged, Closed, or Draft
- **Merged By**: Who merged the PR (if merged)
- **Branches**: Source → Target branch
- **CI Details**: Status of CI/CD checks
- **Conflicts**: Whether merge conflicts exist
- **Out of Date**: If the branch is behind the target
- **Approvals**: Number of approvals received
- **Change Requests**: Number of change requests

### Managing PRs
- **Filter by Status**: Use the tabs to view Open or Merged PRs
- **Mark as Wiki Added**: For merged PRs, click "Add to Wiki" to track documentation

## Troubleshooting

### 403 Forbidden Error
**Error Message:**
```
403 Forbidden: Resource protected by organization SAML enforcement
```

**Solution:**
1. Go to GitHub → Settings → Developer Settings → Personal Access Tokens
2. Find your token and click "Configure SSO"
3. Click "Authorize" next to your organization
4. Restart the application

### 401 Unauthorized Error
**Cause:** Invalid or missing GitHub token

**Solution:**
1. Verify your token is correctly set in `application.properties`
2. Ensure the token hasn't expired
3. Check that the token has `repo` scope

### 404 Not Found Error
**Cause:** PR doesn't exist or you don't have access

**Solution:**
1. Verify the PR URL is correct
2. Ensure you have access to the repository
3. Check if the repository is private and your token has access

### Database Issues
If you see database-related errors:
1. Delete the H2 database file: `rm prtracker-db.mv.db`
2. Restart the application (it will recreate the database)

### Port Already in Use
If port 8080 is already in use, change it in `application.properties`:
```properties
server.port=8081
```

## Configuration Options

### application.properties
```properties
# GitHub API Token
github.token=YOUR_GITHUB_PAT

# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:file:./prtracker-db
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Logging
logging.level.com.example.demo=INFO
logging.level.org.springframework.web=DEBUG
```

## Technologies
- **Spring Boot 3.x**: Main framework
- **Spring Data JPA**: Database access
- **H2 Database**: In-memory/file-based database
- **Thymeleaf**: Template engine for web interface
- **RestTemplate**: GitHub API integration
- **Lombok**: Reduce boilerplate code
- **Maven**: Build tool

## Project Structure
```
demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java          # Main application class
│   │   │   ├── controller/
│   │   │   │   └── PRController.java         # Web endpoints
│   │   │   ├── model/
│   │   │   │   └── PR.java                   # PR entity
│   │   │   ├── repository/
│   │   │   │   └── PRRepository.java         # Database access
│   │   │   └── service/
│   │   │       ├── GithubService.java        # GitHub API integration
│   │   │       └── PRService.java            # Business logic
│   │   └── resources/
│   │       ├── application.properties        # Configuration
│   │       └── templates/
│   │           └── pr_dashboard.html         # Web UI
│   └── test/
└── pom.xml                                   # Maven dependencies
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/prs` | Display PR dashboard |
| POST | `/prs/add` | Add a new PR by URL |
| POST | `/prs/{id}/wiki` | Mark PR as added to wiki |
| POST | `/prs/{id}/delete` | Delete a PR from tracking |

## Example PR URLs
```
https://github.com/extremenetworks/hive-hm/pull/13760
https://github.com/extremenetworks/xcloudiq-api/pull/2165
https://github.com/owner/repository/pull/123
```

## Security Notes
- **Never commit your GitHub token** to version control
- Add `application.properties` to `.gitignore`
- Use environment variables for production deployments:
  ```bash
  export GITHUB_TOKEN=your_token_here
  java -jar demo.jar --github.token=${GITHUB_TOKEN}
  ```

## Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License
MIT

## Support
For issues or questions:
1. Check the Troubleshooting section above
2. Review application logs for detailed error messages
3. Verify your GitHub token has proper permissions and SSO authorization

