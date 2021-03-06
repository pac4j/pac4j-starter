package starter.pac4j;

import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.hbs.Hbs;
import org.jooby.pac4j.Pac4j;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

/**
 * Pac4j starter project.
 */
public class App extends Jooby {

  {
    assets("/css/**");
    assets("/images/**");

    /** Template engine: */
    use(new Hbs());

    get("/login", () -> Results.html("login"));

    use(new Pac4j()
        /** Login with facebook: */
        .client("/facebook", conf ->
            new FacebookClient(conf.getString("fb.key"), conf.getString("fb.secret"))
        )
        /** Login with twitter: */
        .client("/twitter", conf ->
            new TwitterClient(conf.getString("twitter.key"), conf.getString("twitter.secret"))
        )
        /** Login with google: */
        .client("/google", conf -> {
          OidcConfiguration oidc = new OidcConfiguration();
          oidc.setClientId(conf.getString("oidc.clientId"));
          oidc.setSecret(conf.getString("oidc.secret"));
          oidc.setDiscoveryURI(conf.getString("oidc.discoveryURI"));
          oidc.addCustomParam("prompt", "consent");
          return new OidcClient(oidc);
        })
        /** Fallback to form login: */
        .client(conf ->
            new FormClient("/login", ((credentials, context) -> {
              // Create default profile:
              String username = ((UsernamePasswordCredentials) credentials).getUsername();
              final CommonProfile profile = new CommonProfile();
              profile.setId(username);
              profile.addAttribute(Pac4jConstants.USERNAME, username);
              profile.addAttribute(CommonProfileDefinition.DISPLAY_NAME, username);
              credentials.setUserProfile(profile);
            }))
        ));

    /** Protected pages: */
    get("/", "/profile", () -> {
      UserProfile profile = require(UserProfile.class);
      return Results.html("profile").put("profile", profile);
    });
  }

  public static void main(final String[] args) {
    run(App::new, args);
  }

}
