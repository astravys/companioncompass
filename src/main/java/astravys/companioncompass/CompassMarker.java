package astravys.companioncompass;

import java.util.UUID;

public record CompassMarker(
        UUID playerId,
        float x,
        float size,
        float brightness,
        float alpha,
        double distance,
        int color) {
}
