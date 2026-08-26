/**
 * The integration tests that churn thousands of nodes each.
 *
 * <p>
 * The Solr tracker indexes transactions in order, so the class running after one of these waits
 * through its whole backlog in {@link fr.becpg.test.RepoBaseTestCase#waitForSolr()} - that is what
 * used to blow the wait budget of the three classes that happened to follow ECOIT. Surefire orders
 * classes on their fully qualified name, so this package is named to sort after every other test
 * package and the cost stays at the end of the campaign, where nobody pays it.
 * </p>
 *
 * <p>
 * Keep that in mind before adding a test package whose name sorts after this one.
 * </p>
 *
 * @author matthieu
 */
package fr.becpg.test.slow;
