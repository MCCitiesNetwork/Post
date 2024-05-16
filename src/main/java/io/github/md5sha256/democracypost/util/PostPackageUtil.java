package io.github.md5sha256.democracypost.util;

import io.github.md5sha256.democracypost.database.UserState;
import io.github.md5sha256.democracypost.model.PostalPackage;
import org.bukkit.entity.HumanEntity;

public class PostPackageUtil {

    private PostPackageUtil() {
        throw new IllegalStateException("Cannot instantiate static utility class");
    }

    public static void postPackage(HumanEntity who, PostalPackage postalPackage, UserState userState) {
        if (postalPackage.expired()) {
            return;
        }
        InventoryUtil.addItems(who, postalPackage.content().items());
        userState.removePackage(postalPackage.id());
    }

}
