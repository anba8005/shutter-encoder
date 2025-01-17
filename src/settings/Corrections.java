package settings;

import java.io.File;

import application.Shutter;
import library.FFMPEG;
import library.FFPROBE;

public class Corrections extends Shutter {

	public static File vidstab = null;
	
	public static String setStabilisation(String filterComplex, String sequence, File file, String fichier, String concat) throws InterruptedException {
				
		if (grpCorrections.isVisible() && caseStabilisation.isSelected())
		{
			if (System.getProperty("os.name").contains("Windows"))
				vidstab = new File("vidstab.trf");
			else
			{
						    		
				vidstab = new File(Shutter.dirTemp + "vidstab.trf");
			}		
			
			lblCurrentEncoding.setText(Shutter.language.getProperty("analyzeOf") + " " + fichier);
			
			//Analyse du fichier
			String cmd;
			if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux"))
				cmd =  " -an -pix_fmt yuv420p -f yuv4mpegpipe pipe:stab | PathToFFMPEG -i pipe:stab -vf vidstabdetect=result=" + vidstab.toString() + " -y -f null -";					
			else
				cmd =  " -an -pix_fmt yuv420p -f yuv4mpegpipe pipe:stab | PathToFFMPEG -i pipe:stab -vf vidstabdetect=result=" + vidstab.toString() + " -y -f null -" + '"';	
			
			FFMPEG.run(InputAndOutput.inPoint + sequence + concat + " -i " + '"' + file.toString() + '"' + InputAndOutput.outPoint + cmd);		
			
			//Attente de la fin de FFMPEG
			do
				Thread.sleep(100);
			while(FFMPEG.runProcess.isAlive());						
			
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += "vidstabtransform=input=" + vidstab.toString();
			
			lblCurrentEncoding.setText(fichier);
		}
		
		return filterComplex;
	}
	
	public static String setDeflicker(String filterComplex) {
		
		if (grpCorrections.isVisible() && caseDeflicker.isSelected())
		{
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += "split[a][b];[b]setpts=PTS-STARTPTS+" + (1 / FFPROBE.currentFPS) + "/TB,format=rgba,colorchannelmixer=aa=0.5[deflicker];[a][deflicker]overlay=shortest=1";		
		}
		
		return filterComplex;
	}
		
	public static String setDeband(String filterComplex) {
		
		if (grpCorrections.isVisible() && caseBanding.isSelected())
		{
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += "deband=r=32";
		}	
		
		return filterComplex;
	}
	
	public static String setLimiter(String filterComplex) {
		
		if (grpCorrections.isVisible() && caseLimiter.isSelected())
		{			
			if (filterComplex != "") filterComplex += ",";

			if (FFPROBE.imageDepth == 8)
				filterComplex += "limiter=16:235";
			else if (FFPROBE.imageDepth == 10)
				filterComplex += "limiter=64:940";
			else if (FFPROBE.imageDepth == 12)
				filterComplex += "limiter=256:3760";
			else if (FFPROBE.imageDepth == 16)
				filterComplex += "limiter=4096:60160";
		}	
		
		return filterComplex;
	}
	
	public static String setDetails(String filterComplex) {
		
		if (grpCorrections.isVisible() && caseDetails.isSelected())
		{
			float value = (0 - (float) sliderDetails.getValue() / 10);
			
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += "smartblur=1.0:" + value;
		}	
		
		return filterComplex;
	}
	
	public static String setDenoiser(String filterComplex) {
		
		if (grpCorrections.isVisible() && caseBruit.isSelected())
		{
			int value = sliderBruit.getValue();
			
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += "hqdn3d=" + value + ":" + value + ":" + value + ":" + value;
		}
		
		return filterComplex;
	}
	
	public static String setExposure(String filterComplex) {
		
		if (grpCorrections.isVisible() && caseExposure.isSelected())
		{
			int value = sliderExposure.getValue();
			
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += "deflicker=s=" + Math.ceil((128 * value) / 100 + 1);	
		}
		
		return filterComplex;
	}

}
