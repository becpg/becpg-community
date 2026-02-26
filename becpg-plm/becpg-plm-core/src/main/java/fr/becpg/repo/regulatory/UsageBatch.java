package fr.becpg.repo.regulatory;

import java.util.List;

/**
 * <p>UsageBatch class.</p>
 *
 * @param module module
 * @param usages usages
 * @author matthieu
 */
public record UsageBatch(String module, List<String> usages) {

}
