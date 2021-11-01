package world.bentobox.biomes.database.objects;


import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;


/**
 * This class stores necessary information for each Biomes Island Data object.
 */
@Table(name = "BiomesIslandData")
public class BiomesIslandDataObject implements DataObject
{
    /**
     * Empty constructor for loader.
     */
    public BiomesIslandDataObject()
    {
        // Empty constructor.
    }


    /**
     * @return the uniqueId
     */
    @Override
    public String getUniqueId()
    {
        return uniqueId;
    }


    /**
     * @param uniqueId the uniqueId to set
     */
    @Override
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }


    // ---------------------------------------------------------------------
    // Section: Variables
    // ---------------------------------------------------------------------


    /**
     * Unique ID of the island.
     */
    @Expose
    private String uniqueId;
}
