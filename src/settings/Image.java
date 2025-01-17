package settings;

import javax.swing.JComboBox;

import application.Shutter;
import library.FFPROBE;

public class Image extends Shutter {

	public static String setCrop(String filterComplex) {		
		
		if (grpResolution.isVisible() || grpImageSequence.isVisible() || comboFonctions.getSelectedItem().toString().equals("Blu-ray"))
		{
			if (caseRognage.isSelected())
			{
				if (filterComplex != "")
					filterComplex += "[w];[w]";
		
				filterComplex += "crop=" + FFPROBE.cropHeight + ":" + FFPROBE.cropWidth + ":" + FFPROBE.cropPixelsWidth + ":" + FFPROBE.cropPixelsHeight;
			}
	    	
	    	if (caseRognerImage.isSelected())
			{
				if (filterComplex != "")
					filterComplex += "[w];[w]";
				
	    		filterComplex += Shutter.cropFinal;
			}
		}
    	
    	return filterComplex;
	}
	
	public static String setRotate(String filterComplex) {
		
		if (grpResolution.isVisible() || grpImageSequence.isVisible())
		{
			String rotate = "";
			if (caseRotate.isSelected()) 
			{
				String transpose = "";
				switch (comboRotate.getSelectedItem().toString()) {
				case "90":
					if (caseMiror.isSelected())
						transpose = "transpose=3";
					else
						transpose = "transpose=1";
					break;
				case "-90":
					if (caseMiror.isSelected())
						transpose = "transpose=0";
					else
						transpose = "transpose=2";
					break;
				case "180":
					if (caseMiror.isSelected())
						transpose = "transpose=1,transpose=1,hflip";
					else
						transpose = "transpose=1,transpose=1";
					break;
				}
	
				rotate = transpose;
			}
			else if (caseMiror.isSelected())
				rotate = "hflip";
						
			if (rotate != "")
			{
				if (filterComplex != "") filterComplex += ",";
		
				filterComplex += rotate;	
			}
		}

		return filterComplex;
	}
	
	public static String limitToFHD() {
		
		if (comboResolution.getSelectedItem().toString().equals(language.getProperty("source")) == false)
		{
			return " -s "+ comboResolution.getSelectedItem().toString();
		}
		else
			return " -s 1920x1080";
	}
	
	public static String setPad(String filterComplex, boolean isOutputCodec, JComboBox<String> comboScale, boolean limitToFHD) {	
		
		if (isOutputCodec)
		{
			if (lblPad.getText().equals(language.getProperty("lblPad")) && comboH264Taille.getSelectedItem().toString().equals(language.getProperty("source")) == false)
			{
				if (filterComplex != "")
					filterComplex += "[c];[c]";
				
				String s[] = FFPROBE.imageResolution.split("x");
				if (caseRognage.isSelected())
				{
					filterComplex += "pad=" + FFPROBE.imageResolution.replace("x", ":") + ":(ow-iw)*0.5:(oh-ih)*0.5";
				}
				else
				{
					s = comboH264Taille.getSelectedItem().toString().split("x");
					filterComplex += "scale="+s[0]+":"+s[1]+":force_original_aspect_ratio=decrease,pad="+s[0]+":"+s[1]+":(ow-iw)*0.5:(oh-ih)*0.5";
				}
			}
			
			return filterComplex;
		}
		else
		{
			if (comboScale.getSelectedItem().toString().equals(language.getProperty("source")) == false)
			{
				String s[] = comboScale.getSelectedItem().toString().split("x");
				
	        	if (filterComplex != "") filterComplex += ",";
				
				if (lblPad.getText().equals(language.getProperty("lblCrop"))
				|| comboFonctions.getSelectedItem().toString().equals("JPEG")
				|| comboFonctions.getSelectedItem().toString().equals(language.getProperty("functionPicture")))
				{
					if (comboScale.getSelectedItem().toString().contains(":"))
			        {
			        	if (comboScale.getSelectedItem().toString().contains("auto"))
			        	{
			        		s = comboScale.getSelectedItem().toString().split(":");
			        		if (s[0].toString().equals("auto"))
			        			filterComplex = "scale=-1:" + s[1];
			        		else
			        			filterComplex = "scale="+s[0]+":-1";
			        	}
			        	else
			        	{
				            s = comboScale.getSelectedItem().toString().split(":");
				    		float number =  (float) 1 / Integer.parseInt(s[0]);
				    		filterComplex = "scale=iw*" + number + ":ih*" + number;
			        	}
			        }
					else
					{					
						String i[] = FFPROBE.imageResolution.split("x");        	
			
			        	int iw = Integer.parseInt(i[0]);
			        	int ih = Integer.parseInt(i[1]);          	
			        	int ow = Integer.parseInt(s[0]);
			        	int oh = Integer.parseInt(s[1]);        	
			        	float ir = (float) iw / ih;
			        	        	
			        	//Original sup. à la sortie
			        	if (iw > ow || ih > oh)
			        	{
			        		//Si la hauteur calculée est > à la hauteur de sortie
			        		if ( (float) ow / ir >= oh)
			        			filterComplex += "scale=" + ow + ":-1,crop=" + "'" + ow + ":" + oh + ":0:(ih-oh)*0.5" + "'";
			        		else
			        			filterComplex += "scale=-1:" + oh + ",crop=" + "'" + ow + ":" + oh + ":(iw-ow)*0.5:0" + "'";
			        	}
			        	else
			        		filterComplex += "scale=" + ow + ":" + oh;
					}
				}
				else
				{
					if (lblPad.getText().equals(language.getProperty("lblPad")))
					{
						filterComplex += "scale="+s[0]+":"+s[1]+":force_original_aspect_ratio=decrease,pad=" +s[0]+":"+s[1]+":(ow-iw)*0.5:(oh-ih)*0.5";
					}
					else
						filterComplex += "scale="+s[0]+":"+s[1];	
				}
				
			}
			else if (limitToFHD)
			{
				String s[] = "1920x1080".split("x");
				
				if (comboScale.getSelectedItem().toString().equals(language.getProperty("source")) == false)
				{
					s = comboScale.getSelectedItem().toString().split("x");
				}
				
				if (filterComplex != "") filterComplex += ",";
				
				filterComplex += "scale="+s[0]+":"+s[1]+":force_original_aspect_ratio=decrease,pad=" +s[0]+":"+s[1]+":(ow-iw)*0.5:(oh-ih)*0.5";
			}
			else if (comboFilter.getSelectedItem().toString().equals(".ico"))
			{
				filterComplex = "scale=256x256";
			}
		}
		
		return filterComplex;
	}
	
	public static String setDAR(String filterComplex) {
		
		if (grpResolution.isVisible() || grpImageSequence.isVisible())
		{
			if (caseForcerDAR.isSelected())
			{
				if (filterComplex != "") filterComplex += ",";
				
				filterComplex += "setdar=" + comboDAR.getSelectedItem().toString().replace(":", "/");
			}
		}
    	
    	return filterComplex;
	}	
	
}
