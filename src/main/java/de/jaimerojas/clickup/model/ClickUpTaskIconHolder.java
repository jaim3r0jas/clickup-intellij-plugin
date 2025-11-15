package de.jaimerojas.clickup.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ui.ImageUtil;
import de.jaimerojas.clickup.ClickUpRepositoryEditor;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.apache.batik.transcoder.XMLAbstractTranscoder.*;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;
import static org.apache.batik.util.SVGConstants.SVG_SVG_TAG;

public class ClickUpTaskIconHolder {
    private static final int ICON_WIDTH = 8;
    private static final int ICON_HEIGHT = 8;

    public static final BufferedImage clickUpIcon = SvgImageLoader.loadSvg(
            ClickUpTaskIconHolder.class.getClassLoader().getResourceAsStream("icons/logo-v3-clickup-symbol-only.svg"),
            ICON_WIDTH,
            ICON_HEIGHT
    );

    private static class SvgImageLoader {
        private static final Logger LOG = Logger.getInstance(ClickUpRepositoryEditor.class);

        /**
         * Reads an SVG image stream and returns a BufferedImage sized to (width x height).
         * The SVG is rendered at a higher internal resolution and then downscaled with
         * bicubic interpolation to avoid pixelation on small icons.
         */
        public static BufferedImage loadSvg(InputStream svgImage, int width, int height) {
            if (svgImage == null) {
                LOG.warn("SVG input stream is null");
                return ImageUtil.createImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }

            try {
                // Render at a larger internal resolution, then downscale for crisp result.
                final int upscale = 3; // try 2..4 depending on quality/size tradeoff
                float renderWidth = width * (float) upscale;
                float renderHeight = height * (float) upscale;

                SvgTranscoder transcoder = new SvgTranscoder();
                transcoder.setTranscodingHints(getHints(renderWidth, renderHeight));
                TranscoderInput input = new TranscoderInput(svgImage);
                transcoder.transcode(input, null);

                BufferedImage hiRes = transcoder.getImage();
                if (hiRes == null) {
                    throw new TranscoderException("Transcoder returned null image");
                }

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
            } catch (TranscoderException e) {
                LOG.warn("Could not load SVG image", e);
                return ImageUtil.createImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
        }

        private static TranscodingHints getHints(float width, float height) {
            TranscodingHints hints = new TranscodingHints();
            hints.put(KEY_DOM_IMPLEMENTATION, SVGDOMImplementation.getDOMImplementation());
            hints.put(KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVG_NAMESPACE_URI);
            hints.put(KEY_DOCUMENT_ELEMENT, SVG_SVG_TAG);
            hints.put(KEY_WIDTH, width);
            // supply explicit height so aspect ratio and scaling are exact
            hints.put(KEY_HEIGHT, height);
            return hints;
        }

        private static class SvgTranscoder extends ImageTranscoder {

            private BufferedImage image = null;

            @Override
            public BufferedImage createImage(int width, int height) {
                image = ImageUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB);
                return image;
            }

            @Override
            public void writeImage(BufferedImage img, TranscoderOutput out) {
                // nothing to do here; createImage stores the generated image for getImage()
            }

            BufferedImage getImage() {
                return image;
            }
        }
    }

}
