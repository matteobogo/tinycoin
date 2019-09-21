package utilities;

import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

import java.util.List;
import java.util.UUID;

public class Utils {

    public static int getRandomIntInRange(int min, int max) {

        return CommonState.r.nextInt((max - min) + 1) + min;
    }

    public static <T> T getRandomElementFromList(List<T> list) {

        return list.get(CommonState.r.nextInt(list.size()));
    }

    public static Node getRandomNodeFromNetwork() {

        return Network.get(CommonState.r.nextInt(Network.size()));
    }

    public static String getRandomUniqueID() {

        return UUID.randomUUID().toString();
    }
}