
# sca-wrapper

The SCA Wrapper is a HTML layout library that provides play-frontend-hmrc assets to frontend microservices.
The Wrapper provides the HMRC header, footer, components (such as timeout pop-up box, cookie policy pop-up etc.), 
and also provides a menu bar. The current menu bar provided is the same as the Personal Tax Account menu, but this 
design will change in the future. The Wrapper connects with `single-customer-account-wrapper-data` to retrieve the 
latest menu config. The SCA team has the ability to change the menu config without consuming services needing to do 
anything (e.g button position on the menu bar, href links, button names etc. can be updated instantly)

## Using the library with your service

- Import the SCA Library in your SBT Dependencies: "uk.gov.hmrc" %% "sca-wrapper" % "[latest version]"
- Check the `application.conf` file and override the default values
- Inject `WrapperService` and call `layout()`
- Pass your HTML view into the method, and override any parameters as needed

## Testing the library locally and making changes to it

- Import the SCA Library in your SBT Dependencies: "uk.gov.hmrc" %% "sca-wrapper" % "[latest version]-SNAPSHOT"
- Refresh SBT sources
- Clone the SCA library
- Edit the build.sbt file and add `-SNAPSHOT` to the end of the `version` field
- Run `sbt publishLocal`, this will publish the library on your local machine

## Working example

Clone https://github.com/hmrc/single-customer-account-frontend for a working example of the Wrapper being integrated with a standard HMRC MDTP frontend. `HomeController`
shows how the integration works.
To run all the SCA services from service manager, run `sm –start SCA_FUTURES_ALL` or `sm2 –start SCA_FUTURES_ALL`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").