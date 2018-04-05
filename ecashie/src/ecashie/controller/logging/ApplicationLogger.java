package ecashie.controller.logging;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class ApplicationLogger
{
	public static final File logFile = new File("log.html");
	public static Logger logger = Logger.getAnonymousLogger();

	private static ByteArrayOutputStream logStream = new ByteArrayOutputStream();
	private static StreamHandler streamHandler;

	public static String oldLogFileContent = "";

	public static void setup() throws IOException
	{
		initLogger();

		extractOldLogFileContent();

		addLogStreamHandler();

		addLogFileHandler();

		suppressLoggingOutputToConsole();
	}

	private static void initLogger()
	{
		logger.setLevel(Level.ALL);
	}

	private static void suppressLoggingOutputToConsole()
	{
		Logger rootLogger = Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();

		if (handlers[0] instanceof ConsoleHandler)
		{
			rootLogger.removeHandler(handlers[0]);
		}
	}

	private static void extractOldLogFileContent() throws IOException
	{
		if (logFile.exists())
		{
			String oldContent = new String(Files.readAllBytes(logFile.toPath()));

			int startIndex = oldContent.indexOf("<h1>");
			int endIndex = oldContent.indexOf("</body>");

			if (startIndex > -1 && endIndex > -1)
			{
				oldLogFileContent = oldContent.substring(startIndex, endIndex);
			}
		}
	}

	private static void addLogStreamHandler() throws SecurityException
	{
		Handler[] handlers = logger.getParent().getHandlers();
		streamHandler = new StreamHandler(logStream, handlers[0].getFormatter());

		logger.addHandler(streamHandler);
	}

	private static void addLogFileHandler() throws IOException, SecurityException
	{
		FileHandler fileHTML = new FileHandler(logFile.getName(), false);
		Formatter formatterHTML = new HtmlLogFormatter();
		fileHTML.setFormatter(formatterHTML);

		logger.addHandler(fileHTML);
	}

	public static void closeLogger() throws SecurityException
	{
		for (Handler handler : logger.getHandlers())
		{
			handler.flush();
			handler.close();
		}
	}

	public static String getLogAsString()
	{
		streamHandler.flush();

		return logStream.toString();
	}
}
