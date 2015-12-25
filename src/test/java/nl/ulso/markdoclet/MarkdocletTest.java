package nl.ulso.markdoclet;

import test.user.Customer;
import test.user.User;
import test.user.UserType;
import org.junit.Test;

import static java.util.Arrays.asList;

public class MarkdocletTest extends AbstractDocletTestCase {

    @Test
    public void noInputMustResultInNoOutput() throws Exception {
        DocletResult result = runDoclet(asList("-output", "test.md"));
        result.assertFailure();
        result.assertNoOutput();
        result.assertError("No packages or classes specified");
    }

    @Test
    public void generateInterfaceDocumentation() throws Exception {
        final DocletResult result = runDoclet(asList("-output", "test.md"), User.class, Customer.class, UserType.class);
        result.assertSuccess();
        result.assertOutput("# API documentation");
        result.assertNoErrors();
    }

    @Test
    public void customTitleAppearsInOutput() throws Exception {
        final DocletResult result = runDoclet(asList("-output", "test.md", "-title", "User API"),
                UserType.class);
        result.assertSuccess();
        result.assertOutput("# User API");
        result.assertNoErrors();
    }

    @Test
    public void customTagsWithDescriptionsAppearInOutput() throws Exception {
        final DocletResult result = runDoclet(
                asList("-output", "test.md", "-properties", "src/test/resources/tags.properties"),
                UserType.class);
        result.assertSuccess();
        result.assertOutput("If the site is accessed by logged-on users");
        result.assertOutput("If the site is accessed by anonymous users");
        result.assertNoErrors();
    }
}