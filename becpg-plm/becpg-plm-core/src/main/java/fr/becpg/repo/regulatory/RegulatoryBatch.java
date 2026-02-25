package fr.becpg.repo.regulatory;

/**
 * <p>RegulatoryBatch class.</p>
 *
 * @param countryBatches countryBatches
 * @param usageBatches usageBatches
 * @author matthieu
 */
/**
 * <p>countryBatches.</p>
 *
 * @return a {@link fr.becpg.repo.regulatory.CountryBatch} object
 */
/**
 * <p>usageBatches.</p>
 *
 * @return a {@link fr.becpg.repo.regulatory.UsageBatch} object
 */
public record RegulatoryBatch(CountryBatch countryBatches, UsageBatch usageBatches) {

}
