package io.github.md5sha256.democracypost.ui;

import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class UiSettings {


    @Setting
    @Required
    private Material exitButtonMaterial = Material.BARRIER;

    @Setting
    @Required
    private Material backButtonMaterial = Material.BARRIER;

    @Setting
    @Required
    private Material nextButtonMaterial = Material.BOOK;

    @Setting
    @Required
    private Material previousButtonMaterial = Material.BOOK;

    @Setting
    @Required
    private String showPackagesHeadId = "54098";

    @Setting
    @Required
    private String packagesIconHeadId = "54098";

    @Setting
    @Required
    private String collectPackageHeadId = "22840";

    @Setting
    @Required
    private String sendPackageHeadId = "24849";


    public Material exitButtonMaterial() {
        return exitButtonMaterial;
    }

    public Material backButtonMaterial() {
        return backButtonMaterial;
    }

    public Material nextButtonMaterial() {
        return nextButtonMaterial;
    }

    public Material previousButtonMaterial() {
        return previousButtonMaterial;
    }

    public String collectPackageHeadId() {
        return collectPackageHeadId;
    }

    public String showPackagesHeadId() {
        return showPackagesHeadId;
    }

    public String packagesIconHeadId() {
        return packagesIconHeadId;
    }

    public String sendPackageHeadId() {
        return sendPackageHeadId;
    }
}
