package de.jaimerojas.clickup.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.SVGLoader;
import com.intellij.util.ui.ImageUtil;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;

public class ClickUpTaskIconHolder {
    private static final int ICON_WIDTH = 8;
    private static final int ICON_HEIGHT = 8;

    // Load via resource URL (SVGLoader works with URLs) and rasterize to a small BufferedImage
    public static final BufferedImage clickUpIcon = SvgImageLoader.loadSvg(
            ClickUpTaskIconHolder.class.getResource("/icons/logo-v3-clickup-symbol-only.svg"),
            ICON_WIDTH,
            ICON_HEIGHT
    );

    private static class SvgImageLoader {
        // Use this class for logging context related to icon loading
        private static final Logger LOG = Logger.getInstance(ClickUpTaskIconHolder.class);

        /**
         * Reads an SVG resource URL and returns a BufferedImage sized to (width x height).
         * The SVG is rendered at a higher internal resolution and then downscaled with
         * bicubic interpolation to avoid pixelation on small icons.
         */
        @SuppressWarnings("UnstableApiUsage")
        public static BufferedImage loadSvg(URL svgUrl, int width, int height) {
            if (svgUrl == null) {
                LOG.warn("SVG resource URL is null");
                return ImageUtil.createImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }

            try {
                // Render at a larger internal resolution, then downscale for a crisp result.
                final float scale = 3f; // try 2..4 depending on quality/size tradeoff

                // SVGLoader returns an Image; convert to BufferedImage for further processing
                java.awt.Image hiResImage = SVGLoader.load(svgUrl, scale);
                BufferedImage hiRes = ImageUtil.toBufferedImage(hiResImage);

                // Downscale with high-quality rendering hints into the final target size
                BufferedImage finalImage = ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = finalImage.createGraphics();
                try {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.drawImage(hiRes, 0, 0, width, height, null);
                } finally {
                    g.dispose();
                }
                return finalImage;
            } catch (Exception e) {
                LOG.warn("Could not load SVG image", e);
                return ImageUtil.createImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
        }
    }

}
