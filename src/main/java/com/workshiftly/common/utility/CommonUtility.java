
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.common.utility;

import com.workshiftly.service.logger.InternalLogger;
import com.workshiftly.service.logger.LoggerService;
import java.awt.image.BufferedImage;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.apache.commons.lang3.BooleanUtils;
/**
 *
 * @author chamara
 */
public class CommonUtility {
    private static final InternalLogger LOGGER = LoggerService.getLogger(CommonUtility.class);

    /**
     * Method Name: parseToBoolean
     * Description: parse Integer or String or Boolean value into primitive boolean
     *      See javadoc published for apache commons lang3 BooleanUtils
     * @param input
     * @return boolean true or false
     */
    public static boolean parseToBoolean(Object input) {
        
        if (input instanceof Integer) {
            return BooleanUtils.toBoolean((int) input);
        }
        if (input instanceof String) {
            return BooleanUtils.toBoolean((String) input);
        }
        
        if (input instanceof Boolean) {
            return BooleanUtils.toBoolean((Boolean) input);
        }
        throw new UnsupportedOperationException("Class type not supported");
    }
    
    public static Image convertToFxImage(BufferedImage image) {
        WritableImage wr = null;
        if (image != null) {
            wr = new WritableImage(image.getWidth(), image.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }
        return new ImageView(wr).getImage();
    }
    
    public static String removeWhitespaces(String input) {
        return input.replaceAll("//s", "");
    }
    
    // convert bytes into human readable format literals
    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }
}
