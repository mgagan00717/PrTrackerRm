# Quick Setup Guide - GitHub PR Tracker

## 🚨 IMPORTANT: SSO Authorization Required!

If you're getting **403 Forbidden** errors, your GitHub token needs SSO authorization.

## Step-by-Step Setup

### Step 1: Create GitHub Personal Access Token

1. Go to: https://github.com/settings/tokens
2. Click **"Generate new token (classic)"**
3. Name it: `PR Tracker Tool`
4. Select scopes:
   - ✅ `repo` (all repo permissions)
   - ✅ `read:org`
5. Click **"Generate token"**
6. **COPY THE TOKEN IMMEDIATELY** (you won't see it again!)

### Step 2: Authorize SSO (CRITICAL!)

🔴 **This is the step most people miss!**

After creating the token:

1. Stay on the tokens page
2. Find your newly created token
3. Look for **"Configure SSO"** or **"Enable SSO"** button next to it
4. Click **"Authorize"** next to `extremenetworks` (or your organization)
5. You may need to re-authenticate with your SSO provider
6. Confirm the authorization

**Alternative Method:**
- Visit: https://github.com/orgs/extremenetworks/sso
- Follow the authorization flow
- Select your token to authorize

### Step 3: Configure Application

1. Open `src/main/resources/application.properties`
2. Replace the placeholder:
   ```properties
   github.token=ghp_your_actual_token_here
   ```
3. Save the file

### Step 4: Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Step 5: Test

1. Open browser: http://localhost:8080/prs
2. Enter a PR URL: `https://github.com/extremenetworks/xcloudiq-api/pull/2165`
3. Click "Add PR"
4. ✅ Should fetch PR details successfully!

## Common Errors and Solutions

### Error: 403 Forbidden - SAML Enforcement
```
Resource protected by organization SAML enforcement
```
**Fix:** Follow Step 2 above to authorize SSO

### Error: 401 Unauthorized - Bad Credentials
```
401 Unauthorized: Bad credentials
```
**Fix:** 
- Check token is correct in application.properties
- Ensure token hasn't expired
- Regenerate if needed

### Error: 404 Not Found
```
404 Not Found
```
**Fix:**
- Verify PR URL is correct
- Check you have access to the repository
- Ensure token has `repo` scope

## Verification Checklist

Before running the app, verify:

- [ ] GitHub PAT created with `repo` scope
- [ ] SSO authorized for your organization
- [ ] Token added to `application.properties`
- [ ] File saved with actual token (not placeholder)
- [ ] Maven build successful
- [ ] Port 8080 is available

## Testing Your Token

You can test if your token works with curl:

```bash
curl -H "Authorization: token YOUR_TOKEN_HERE" \
  https://api.github.com/repos/extremenetworks/xcloudiq-api/pulls/2165
```

✅ **Success:** You'll see PR JSON data
❌ **Failure:** You'll see 401/403 error

## Security Best Practices

1. **Never commit tokens to git**
   - Add `application.properties` to `.gitignore`
   
2. **Use environment variables in production:**
   ```bash
   export GITHUB_TOKEN=your_token
   java -jar demo.jar --github.token=${GITHUB_TOKEN}
   ```

3. **Rotate tokens periodically**

4. **Use fine-grained tokens when possible**
   - Go to: https://github.com/settings/tokens?type=beta
   - Select specific repositories
   - Set expiration date

## Need Help?

1. Check application logs in console
2. Enable DEBUG logging in `application.properties`:
   ```properties
   logging.level.com.example.demo=DEBUG
   ```
3. Review the Troubleshooting section in README.md

## Quick Reference

| URL | Purpose |
|-----|---------|
| http://localhost:8080/prs | Main dashboard |
| http://localhost:8080/h2-console | Database console |
| https://github.com/settings/tokens | Manage PATs |
| https://github.com/orgs/extremenetworks/sso | Authorize SSO |

---

**Still having issues?** 
- Verify SSO authorization was completed
- Check token hasn't expired
- Try creating a new token and re-authorizing

