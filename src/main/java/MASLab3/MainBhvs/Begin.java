package MASLab3.MainBhvs;

import MASLab3.Etc.BehaviourKiller;
import MASLab3.Etc.Link;
import MASLab3.Etc.Setting;
import MASLab3.Etc.WorkWithConfigFiles;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.apache.commons.lang.math.IntRange;

import javax.management.RuntimeErrorException;
import java.util.Scanner;

import java.util.List;

public class Begin extends OneShotBehaviour {

    private Agent agent;
    private List<AID> receivers;
    private boolean linkFound = false;
    private String agentTemplate;

    public Begin(Agent agent, DataStore dataStore) {
        setDataStore(dataStore);
        receivers = (List<AID>) dataStore.get("receivers");
        this.agent = agent;
    }

    @Override
    public void action() {
        Setting setting = WorkWithConfigFiles.unMarshalAny(Setting.class, agent.getName() + ".xml");
        List<Link> links = setting.getLinks();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Agent " + agent.getLocalName() + " said: Enter the tag of agent to be found (INTEGER): ");
        int input = scanner.nextInt();
        agentTemplate = "Agent " + input;
        if (new IntRange(0, 10).containsInteger(input)) {
            int i = 0;
            while (i <= links.size() && (linkFound == false)) {
                if (links.get(i).getAgentName().equals(agentTemplate)) {
                    System.out.println("Agent " + agent.getLocalName() + " found link with "
                            + links.get(i).getAgentName() + ". The line weight is " + links.get(i).getWeight());
                } else {
                    i = i + 1;
                }
            }
            if (linkFound == true) {
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.setProtocol("foundLink");
                message.setContent(agentTemplate);
                for (AID rec : receivers) {
                    for (int j = 0; j < links.size(); j++) {
                        if (rec.getLocalName().equals(links.get(j).getAgentName())) {
                            message.addReceiver(rec);
                        }
                    }
                }
                agent.send(message);
                System.out.println("Agent " + agent.getLocalName() + " said: I've sent a request!");
            }
        } else {
            throw new RuntimeException("Error! What the fuck you've entered?");
        }
    }

    @Override
    public int onEnd() {
        FindingTheShortest behaviourToKill = new FindingTheShortest(agent);
        agent.addBehaviour(behaviourToKill);
        agent.addBehaviour(new BehaviourKiller(agent, 1000, behaviourToKill));
        return super.onEnd();
    }
}