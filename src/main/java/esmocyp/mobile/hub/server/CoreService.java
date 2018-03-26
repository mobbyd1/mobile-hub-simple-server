package esmocyp.mobile.hub.server;

import com.google.gson.JsonObject;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.ClientLibProtocol;
import lac.cnclib.sddl.serialization.Serialization;
import lac.cnet.sddl.objects.ApplicationObject;
import lac.cnet.sddl.objects.Message;
import lac.cnet.sddl.objects.PrivateMessage;
import lac.cnet.sddl.udi.core.SddlLayer;
import lac.cnet.sddl.udi.core.UniversalDDSLayerFactory;
import lac.cnet.sddl.udi.core.listener.UDIDataReaderListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * Created by ruhan on 25/03/18.
 */
@Service
public class CoreService implements UDIDataReaderListener<ApplicationObject> {

    private Object receiveMessageTopic;
    private SddlLayer core;


    @PostConstruct
    public void init() {

        // Create a layer and participant
        core = UniversalDDSLayerFactory.getInstance( UniversalDDSLayerFactory.SupportedDDSVendors.OpenSplice );
        core.createParticipant( UniversalDDSLayerFactory.CNET_DOMAIN );

        // Receive and write topics to domain
        core.createSubscriber();

        // ClientLib Events
        receiveMessageTopic = core.createTopic( Message.class, Message.class.getSimpleName() );
        core.createDataReader( this, receiveMessageTopic );

        initMEPAQuery();
    }

    private void initMEPAQuery() {
        final JsonObject mepaQuery = new JsonObject();
        final JsonObject options = new JsonObject();

        final String query = "";

        options.addProperty( "type", "add" );
        options.addProperty( "label", "TemperatureMonitor" );
        options.addProperty( "object", "rule" );
        options.addProperty( "rule", query );
        options.addProperty( "target", "local" );

        mepaQuery.add( "MEPAQuery", options );

        // Send the message
        final ApplicationMessage appMsg = new ApplicationMessage();
        appMsg.setPayloadType( ClientLibProtocol.PayloadSerialization.JSON );
        appMsg.setContentObject( "[" + mepaQuery.toString() + "]" );

        sendUnicastMSG( appMsg, null );
    }

    /**
     * Sends a message to a unique component (UNICAST)
     * @param appMSG The application message (e.g. a String message)
     * @param nodeID The UUID of the receiver
     */
    public void sendUnicastMSG( ApplicationMessage appMSG, UUID nodeID ) {
        PrivateMessage privateMSG = new PrivateMessage();
        privateMSG.setGatewayId( UniversalDDSLayerFactory.BROADCAST_ID );
        privateMSG.setNodeId( nodeID );
        privateMSG.setMessage( Serialization.toProtocolMessage( appMSG ) );

        sendCoreMSG( privateMSG );
    }

    /**
     * Writes the message (send)
     * @param privateMSG The message
     */
    private void sendCoreMSG( PrivateMessage privateMSG ) {
        core.writeTopic( PrivateMessage.class.getSimpleName(), privateMSG );
    }

    @Override
    public void onNewData( ApplicationObject applicationObject ) {
        Message message = null;

        if( applicationObject instanceof Message ) {
            message = ( Message ) applicationObject;

            String content = new String( message.getContent() );
            JSONParser parser = new JSONParser();

            try {
                JSONObject object = (JSONObject) parser.parse( content );
                String tag = (String) object.get( "tag" );

                switch( tag ) {

                    case "EventData":
                        final String label = (String) object.get( "label" );
                        final JSONObject jsonObject = ( JSONObject ) object.get("data");

                        //handleEvent( label, jsonObject );
                        break;

                }
            } catch( Exception ex ) {
                System.out.println( ex.getMessage() );
            }
        }
    }

    private void handleEvent(  ) {

    }
}
