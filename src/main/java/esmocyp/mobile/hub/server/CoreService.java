package esmocyp.mobile.hub.server;

import com.google.gson.JsonObject;
import esmocyp.mobile.hub.model.TemperatureType;
import esmocyp.mobile.hub.reasoning.ReasoningServiceFacade;
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
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by ruhan on 25/03/18.
 */
@Service
public class CoreService implements UDIDataReaderListener<ApplicationObject> {

    private Object receiveMessageTopic;
    private SddlLayer core;

    @Autowired
    ReasoningServiceFacade reasoningServiceFacade;

    @PostConstruct
    public void init() throws IOException {

        // Create a layer and participant
        core = UniversalDDSLayerFactory.getInstance( UniversalDDSLayerFactory.SupportedDDSVendors.OpenSplice );
        core.createParticipant( UniversalDDSLayerFactory.CNET_DOMAIN );

        // Receive and write topics to domain
        core.createSubscriber();

        // ClientLib Events
        receiveMessageTopic = core.createTopic( Message.class, Message.class.getSimpleName() );
        core.createDataReader( this, receiveMessageTopic );

        reasoningServiceFacade.initService();
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

    /**
     * Handle different events identified by a label
     * @param label The identifier of the event
     * @param data The data content of the event in JSON
     * @throws ParseException
     */
    private void handleEvent(
            final String label
            , final String uuid
            , final JSONObject data ) throws IOException {

        System.out.println( "\n===========================" );

        switch( label ) {
            case "HeatIndex":
                Double heat = ( Double ) data.get("value");

                String message = null;
                if( heat >= 80 ) {
                    reasoningServiceFacade.stream("sala1", uuid, TemperatureType.VERY_HOT);

                } else if( heat > 90 && heat <= 105 ) {
                    message = "Heat: Extreme Caution";

                } else if( heat > 105 && heat <= 130 ) {
                    message = "Heat: Danger";

                } else if( heat > 130 ) {
                    message = "Heat: Extreme Danger";

                }

                System.out.println( heat );
                break;

            default:
                break;
        }

        System.out.println( "===========================\n" );
    }

    @Override
    public void onNewData( ApplicationObject topicSample ) {
        Message msg = null;

        if( topicSample instanceof Message ) {
            msg = (Message) topicSample;

            String content = new String( msg.getContent() );
            JSONParser parser = new JSONParser();

            try {
                JSONObject object = (JSONObject) parser.parse( content );
                String tag = (String) object.get( "tag" );

                switch( tag ) {
                    case "SensorData":
                        break;

                    case "EventData":
                        final String label = (String) object.get( "label" );
                        final String uuid = (String) object.get("uuid");
                        final JSONObject jsonObject = ( JSONObject ) object.get("data");

                        handleEvent( label, uuid, jsonObject );
                        break;

                    case "LocationData":
                        final Double longitude = (Double) object.get("longitude");
                        final Double latitude = (Double) object.get("latitude");

                        final String latLong = String.format("Longitude: %s | Latitude: %s", longitude, latitude);
                        System.out.println( latLong );

                        break;

                    case "ReplyData":
                    case "ErrorData":
                        break;
                }
            } catch( Exception ex ) {
                System.out.println( ex.getMessage() );
            }
        }
    }
}
