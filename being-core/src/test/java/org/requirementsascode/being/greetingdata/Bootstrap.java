package org.requirementsascode.being.greetingdata;

import org.requirementsascode.being.Beings;
import org.requirementsascode.being.HttpRequestHandlers;
import org.requirementsascode.being.QueryModel;

import io.vlingo.xoom.http.resource.Configuration.Sizing;
import io.vlingo.xoom.http.resource.Configuration.Timing;
import io.vlingo.xoom.http.resource.Resources;
import io.vlingo.xoom.http.resource.Server;
import io.vlingo.xoom.lattice.grid.Grid;
import io.vlingo.xoom.turbo.Boot;

@SuppressWarnings("all")
public class Bootstrap {
	public static final String CREATE_PATH = "/greetings";
	public static final String UPDATE_PATH = "/greetings/change/{id}";
	public static final String FIND_BY_ID_PATH = "/greetings/{id}";
	public static final String FIND_ALL_PATH = "/greetings";

	private final Grid grid;
	private final Server server;
	private static Bootstrap instance;

	public Bootstrap(final String nodeName) throws Exception {
		grid = Boot.start("being", nodeName);
		
		QueryModel<GreetingData> queryModel = 
			QueryModel.startEmpty(GreetingData.empty()) 
				.mergeEventsOf(GreetingCreated.class, 
					(event,previousData) -> GreetingData.from(event.id, event.salutation, event.personName))
				.mergeEventsOf(SalutationChanged.class, 
					(event,previousData) -> GreetingData.from(event.id, event.salutation, previousData.personName));
		
		Beings.baseOn(grid, queryModel);
		
		HttpRequestHandlers<GreetingCommand, GreetingState, GreetingData> greetingRequestHandlers = 
			HttpRequestHandlers.builder()
				.stage(grid)
				.behaviorSupplier(() -> new Greeting())
				.queryDataFromState(GreetingData::from)
				.createRequest(CREATE_PATH, CreateGreeting.class)
				.updateRequest(UPDATE_PATH, ChangeSalutation.class)
				.findByIdRequest(FIND_BY_ID_PATH)
				.findAllRequest(FIND_ALL_PATH)
				.build();

		Resources allResources = Resources.are(greetingRequestHandlers.routes());

		server = Server.startWith(grid.world().stage(), allResources, Boot.serverPort(), Sizing.define(),
				Timing.define());

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (instance != null) {
				instance.server.stop();

				System.out.println("\n");
				System.out.println("=========================");
				System.out.println("Stopping being.");
				System.out.println("=========================");
			}
		}));
	}

	public void stopServer() {
		if (instance == null) {
			throw new IllegalStateException("being server not running");
		}
		instance.server.stop();

		instance = null;
	}

	public Grid grid() {
		return grid;
	}

	public static void main(final String[] args) throws Exception {
		System.out.println("=========================");
		System.out.println("service: being.");
		System.out.println("=========================");

		instance = new Bootstrap(parseNodeName(args));
	}

	private static String parseNodeName(final String[] args) {
		if (args.length == 0) {
			return null;
		} else if (args.length > 1) {
			System.out.println("Too many arguments; provide node name only.");
			System.exit(1);
		}
		return args[0];
	}
}
