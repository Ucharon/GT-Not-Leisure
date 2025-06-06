package com.science.gtnl.common.machine.OreProcessing;

import static com.science.gtnl.Utils.Utils.copyAmount;
import static com.science.gtnl.Utils.Utils.setStackSize;
import static com.science.gtnl.common.machine.OreProcessing.OP_Values.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;

import com.google.common.collect.Sets;
import com.science.gtnl.ScienceNotLeisure;
import com.science.gtnl.loader.RecipeRegister;

import bartworks.system.material.WerkstoffLoader;
import goodgenerator.items.GGMaterial;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gtnhlanth.common.register.WerkstoffMaterialPool;
import ic2.core.Ic2Items;

public class OP_NormalProcessing {

    /**
     * Ore stone types enum
     */
    public final Set<OrePrefixes> basicStoneTypes = Sets.newHashSet(
        OrePrefixes.ore,
        OrePrefixes.rawOre,
        OrePrefixes.oreBasalt,
        OrePrefixes.oreBlackgranite,
        OrePrefixes.oreRedgranite,
        OrePrefixes.oreMarble,
        OrePrefixes.oreNetherrack,
        OrePrefixes.oreEndstone);

    public final Set<OrePrefixes> basicStoneTypesExceptNormalStone = Sets.newHashSet(
        OrePrefixes.oreBasalt,
        OrePrefixes.rawOre,
        OrePrefixes.oreBlackgranite,
        OrePrefixes.oreRedgranite,
        OrePrefixes.oreMarble,
        OrePrefixes.oreNetherrack,
        OrePrefixes.oreEndstone);

    public final Map<Materials, ItemStack> processingLineMaterials = new HashMap<>();

    public void initProcessingLineMaterials() {
        processingLineMaterials.put(Materials.Platinum, WerkstoffLoader.PTMetallicPowder.get(OrePrefixes.dust, 1));
        processingLineMaterials.put(Materials.Palladium, WerkstoffLoader.PDMetallicPowder.get(OrePrefixes.dust, 1));
        processingLineMaterials.put(Materials.Iridium, WerkstoffLoader.IrLeachResidue.get(OrePrefixes.dust, 1));
        processingLineMaterials.put(Materials.Osmium, WerkstoffLoader.IrOsLeachResidue.get(OrePrefixes.dust, 1));
        processingLineMaterials
            .put(Materials.Samarium, WerkstoffMaterialPool.SamariumOreConcentrate.get(OrePrefixes.dust, 1));
        processingLineMaterials
            .put(Materials.Cerium, WerkstoffMaterialPool.CeriumOreConcentrate.get(OrePrefixes.dust, 1));
    }

    // public final List<Integer> insteadMaterialOresMetas = Arrays.asList(
    // 19, 20, 28, 32, 33, 35, 57, 86, 89, 98, 347, 382, 500, 501, 514, 522, 526, 530,
    // 535, 540, 541, 542, 543, 544, 545, 770, 810, 817, 826, 884, 894, 918, 920
    // );

    public ItemStack getDustStack(Materials material, int amount) {
        if (SpecialProcessingLineMaterialInstead) {
            ItemStack t = processingLineMaterials.get(material);
            if (t != null) {
                return copyAmount(Integer.MAX_VALUE, t);
            }
        }
        return setStackSize(GTOreDictUnificator.get(OrePrefixes.dust, material, 1), amount);
    }

    /**
     * Generate recipes.
     */
    public void enumOreProcessingRecipes() {
        initProcessingLineMaterials();
        Set<Materials> specialProcesses = Sets.newHashSet(
            Materials.Samarium,
            Materials.Cerium,
            Materials.Naquadah,
            Materials.NaquadahEnriched,
            Materials.Naquadria);

        // generate normal materials' ore processing recipes
        for (int i = 0; i < GregTechAPI.sGeneratedMaterials.length; i++) {
            if (GregTechAPI.sGeneratedMaterials[i] == null) continue;

            Materials material = GregTechAPI.sGeneratedMaterials[i];

            // rule out special materials
            if (!specialProcesses.isEmpty() && specialProcesses.contains(material)) {
                specialProcesses.remove(material);
                continue;
            }
            // generate recipes
            processOreRecipe(material, i);
        }

        processSpecialOreRecipe();
        new OP_GTPP_OreHandler().processGTPPOreRecipes();
        new OP_GTPP_OreHandler().processGTPPRawOreRecipes();
        new OP_Bartworks_OreHandler().processBWOreRecipes();

    }

    /**
     * Generate special ores recipes
     */
    public void processSpecialOreRecipe() {
        // spotless:off

        // Cerium ore
        {
            ItemStack[] outputs = new ItemStack[] {
                WerkstoffMaterialPool.CeriumOreConcentrate.get(OrePrefixes.dust, Integer.MAX_VALUE) };
            ItemStack[] outputsRich = new ItemStack[] {
                WerkstoffMaterialPool.CeriumOreConcentrate.get(OrePrefixes.dust, Integer.MAX_VALUE) };
            for (OrePrefixes prefixes : basicStoneTypes) {
                if (GTOreDictUnificator.get(prefixes, Materials.Cerium, 1) == null) continue;
                registryOreProcessRecipe(
                    GTOreDictUnificator.get(prefixes, Materials.Cerium, 1),
                    isRich(prefixes) ? outputsRich : outputs
                );
            }
        }

        // Samarium Ore
        {
            ItemStack[] outputs = new ItemStack[] {
                WerkstoffMaterialPool.SamariumOreConcentrate.get(OrePrefixes.dust, Integer.MAX_VALUE) };
            ItemStack[] outputsRich = new ItemStack[] {
                WerkstoffMaterialPool.SamariumOreConcentrate.get(OrePrefixes.dust, Integer.MAX_VALUE) };
            for (OrePrefixes prefixes : basicStoneTypes) {
                if (GTOreDictUnificator.get(prefixes, Materials.Samarium, 1) == null) continue;
                registryOreProcessRecipe(
                    GTOreDictUnificator.get(prefixes, Materials.Samarium, 1),
                    isRich(prefixes) ? outputsRich : outputs
                );
            }
        }

        // Naquadah Ore
        {
            ItemStack[] outputs = new ItemStack[] { GGMaterial.naquadahEarth.get(OrePrefixes.dust, Integer.MAX_VALUE),
                GGMaterial.enrichedNaquadahEarth.get(OrePrefixes.dust, Integer.MAX_VALUE), };
            ItemStack[] outputsRich = new ItemStack[] { GGMaterial.naquadahEarth.get(OrePrefixes.dust, Integer.MAX_VALUE),
                GGMaterial.enrichedNaquadahEarth.get(OrePrefixes.dust, Integer.MAX_VALUE), };
            for (OrePrefixes prefixes : basicStoneTypes) {
                if (GTOreDictUnificator.get(prefixes, Materials.Naquadah, 1) == null) continue;
                registryOreProcessRecipe(
                    GTOreDictUnificator.get(prefixes, Materials.Naquadah, 1),
                    isRich(prefixes) ? outputsRich : outputs
                );
            }
        }

        // Enriched Naquadah Ore
        {
            ItemStack[] outputs = new ItemStack[] { GGMaterial.enrichedNaquadahEarth.get(OrePrefixes.dust, Integer.MAX_VALUE),
                GGMaterial.naquadriaEarth.get(OrePrefixes.dust, Integer.MAX_VALUE) };
            ItemStack[] outputsRich = new ItemStack[] { GGMaterial.enrichedNaquadahEarth.get(OrePrefixes.dust, Integer.MAX_VALUE),
                GGMaterial.naquadriaEarth.get(OrePrefixes.dust, Integer.MAX_VALUE) };
            for (OrePrefixes prefixes : basicStoneTypes) {
                if (GTOreDictUnificator.get(prefixes, Materials.NaquadahEnriched, 1) == null) continue;
                registryOreProcessRecipe(
                    GTOreDictUnificator.get(prefixes, Materials.NaquadahEnriched, 1),
                    isRich(prefixes) ? outputsRich : outputs
                );
            }
        }

        // Naquadria Ore
        {
            ItemStack[] outputs = new ItemStack[] { GGMaterial.naquadriaEarth.get(OrePrefixes.dust, Integer.MAX_VALUE),
                GGMaterial.naquadriaEarth.get(OrePrefixes.dust, Integer.MAX_VALUE), };
            ItemStack[] outputsRich = new ItemStack[] { GGMaterial.naquadriaEarth.get(OrePrefixes.dust, Integer.MAX_VALUE),
                GGMaterial.naquadriaEarth.get(OrePrefixes.dust, Integer.MAX_VALUE), };
            for (OrePrefixes prefixes : basicStoneTypes) {
                if (GTOreDictUnificator.get(prefixes, Materials.Naquadria, 1) == null) continue;
                registryOreProcessRecipe(
                    GTOreDictUnificator.get(prefixes, Materials.Naquadria, 1),
                    isRich(prefixes) ? outputsRich : outputs
                );
            }
        }

        // Tinker Construct
        // Cobalt ore
        processOreRecipe(
            GTModHandler.getModItem("TConstruct","SearedBrick", 1, 1),
            Materials.Cobalt,
            true
        );

        // Ardite ore
        processOreRecipe(
            GTModHandler.getModItem("TConstruct","SearedBrick", 1, 2),
            Materials.Ardite,
            true
        );

        // IC2 Uranium ore
        processOreRecipe(
            copyAmount(1,Ic2Items.uraniumOre),
            Materials.Uranium,
            false
        );

        // HEE end powder
        registryOreProcessRecipe(
            GTModHandler.getModItem("HardcoreEnderExpansion","end_powder_ore",1),
            new ItemStack[]{GTModHandler.getModItem("HardcoreEnderExpansion", "end_powder", 24)}
        );

        // spotless:on
    }

    /**
     * Generate normal ore recipes
     *
     * @param material The ore's Material.
     * @param ID       The material ID.
     */
    public void processOreRecipe(Materials material, int ID) {
        if (GTOreDictUnificator.get(OrePrefixes.ore, material, 1) == null) return;
        ItemStack[] outputs = getOutputs(material, false);
        ItemStack[] outputsRich = getOutputs(material, true);

        // registry normal stone ore
        registryOreProcessRecipe(GTModHandler.getModItem("gregtech", "gt.blockores", 1, ID), outputs);

        // registry gt stone ore
        for (OrePrefixes prefixes : basicStoneTypesExceptNormalStone) {
            if (GTOreDictUnificator.get(prefixes, material, 1) == null) {
                ScienceNotLeisure.LOG.info("Failed to get ore: material=" + material + " , prefixes=" + prefixes);
                continue;
            }
            registryOreProcessRecipe(
                GTOreDictUnificator.get(prefixes, material, 1),
                isRich(prefixes) ? outputsRich : outputs);
        }

        if (GTOreDictUnificator.get(OrePrefixes.rawOre, material, 1) != null) {
            registryOreProcessRecipe(
                GTOreDictUnificator.get(OrePrefixes.rawOre, material, 1),
                isRich(OrePrefixes.rawOre) ? outputsRich : outputs);
        }
    }

    /**
     * Process other mods' ore but normal style.
     *
     * @param inputOreItems Input ore item stack.
     * @param material      Input ore's material in GT design.
     * @param isRich        Is this ore a rich type.
     */
    public void processOreRecipe(ItemStack inputOreItems, Materials material, boolean isRich) {
        registryOreProcessRecipe(inputOreItems, getOutputs(material, isRich));
    }

    public ItemStack[] getOutputs(Materials material, boolean isRich) {
        List<ItemStack> outputs = new ArrayList<>();

        // check byproduct
        if (!material.mOreByProducts.isEmpty()) {
            // the basic output the material
            outputs.add(getDustStack(material, Integer.MAX_VALUE));
            if (material.mOreByProducts.size() == 1) {
                for (Materials byproduct : material.mOreByProducts) {
                    if (byproduct == null) continue;
                    outputs.add(getDustStack(byproduct, Integer.MAX_VALUE));
                }
            } else {
                for (Materials byproduct : material.mOreByProducts) {
                    if (byproduct == null || byproduct == Materials.Netherrack
                        || byproduct == Materials.Endstone
                        || byproduct == Materials.Stone) continue;

                    outputs.add(getDustStack(byproduct, Integer.MAX_VALUE));
                }
            }

        } else {
            outputs.add(getDustStack(material, Integer.MAX_VALUE));
        }

        // check gem style
        if (GTOreDictUnificator.get(OrePrefixes.gem, material, 1) != null) {
            if (GTOreDictUnificator.get(OrePrefixes.gemExquisite, material, 1) != null) {
                // has gem style
                outputs.add(GTOreDictUnificator.get(OrePrefixes.gemExquisite, material, Integer.MAX_VALUE));
                outputs.add(GTOreDictUnificator.get(OrePrefixes.gemFlawless, material, Integer.MAX_VALUE));
                outputs.add(GTOreDictUnificator.get(OrePrefixes.gem, material, Integer.MAX_VALUE));

            } else {
                // just normal gem
                outputs.add(GTOreDictUnificator.get(OrePrefixes.gem, material, Integer.MAX_VALUE));
            }
        }

        if (isRich) {
            for (ItemStack out : outputs) {
                out.stackSize = Integer.MAX_VALUE;
            }
        }

        return outputs.toArray(new ItemStack[0]);
    }

    public void registryOreProcessRecipe(ItemStack input, ItemStack[] output) {
        GTValues.RA.stdBuilder()
            .itemInputs(input)
            .itemOutputs(output)
            .noOptimize()
            .eut(OreProcessRecipeEUt)
            .duration(OreProcessRecipeDuration)
            .addTo(RecipeRegister.CheatOreProcessingRecipes);
    }

    /**
     * Check is this OrePrefix is rich ore style.
     *
     * @param prefixes The style to check.
     * @return True is rich ore.
     */
    public boolean isRich(OrePrefixes prefixes) {
        return prefixes == OrePrefixes.oreNetherrack || prefixes == OrePrefixes.oreEndstone;
    }

}
