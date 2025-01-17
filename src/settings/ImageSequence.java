package settings;

import java.io.File;

import application.Shutter;

public class ImageSequence extends Shutter {

	public static String setSequence(File file, String extension) {
		
		if (grpImageSequence.isVisible() && caseEnableSequence.isSelected())
		{
			int n = 0;
			do {
				n ++;
			} while (file.toString().substring(file.toString().lastIndexOf(".") - n).replace(extension, "").matches("[0-9]+") != false);					

			if (caseBlend.isSelected())
				return " -start_number " + file.toString().substring(file.toString().lastIndexOf(".") - n + 1).replace(extension, "");	
			else	
				return " -framerate " + caseSequenceFPS.getSelectedItem().toString().replace(",", ".") + " -start_number " + file.toString().substring(file.toString().lastIndexOf(".") - n + 1).replace(extension, "");	
		}		
		
		return "";
	}
	
	public static File setSequenceName(File file, String extension) {
		
		if (grpImageSequence.isVisible() && caseEnableSequence.isSelected())
		{
			int n = 0;
			do {
				n ++;
			} while (file.toString().substring(file.toString().lastIndexOf(".") - n).replace(extension, "").matches("[0-9]+") != false);	
			
			int nombre = (n - 1);
			file = new File(file.toString().substring(0, file.toString().lastIndexOf(".") - nombre) + "%0" + nombre + "d" + extension);				
		}
		
		return file;
		
	}
	
	public static String setBlend(String filterComplex) {
		
		if (grpImageSequence.isVisible() && caseBlend.isSelected())
		{			
			int value = sliderBlend.getValue();
			StringBuilder blend = new StringBuilder();
			for (int i = 0 ; i < value ; i++)
			{
				blend.append("tblend=all_mode=average,");
			}	
			
			blend.append("setpts=" + (float) 25 / Float.parseFloat(caseSequenceFPS.getSelectedItem().toString().replace(",", ".")) + "*PTS");
			
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += blend;	
		}
		
		return filterComplex;
	}
	
	public static String setMotionBlur(String filterComplex) {
		
		if (grpImageSequence.isVisible() && caseMotionBlur.isSelected())
		{			
			float fps = Float.parseFloat(caseSequenceFPS.getSelectedItem().toString().replace(",", ".")) * 2;
			
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += "minterpolate=fps=" + fps + ",tblend=all_mode=average,framestep=2";	
		}
		
		return filterComplex;
	}
	
}
