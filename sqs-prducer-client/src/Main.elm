module Main exposing (main)

import Browser
import Html exposing (Html, button, input, li, main_, p, text, ul)
import Html.Attributes exposing (class, value)
import Html.Events exposing (onClick, onInput)
import Http
import Json.Decode as JD
import Json.Encode as JE
import Task
import Time



-- MAIN


main : Program () Model Msg
main =
    Browser.element
        { init = init
        , update = update
        , view = view
        , subscriptions = subscriptions
        }



-- MODEL


type alias SendMessageResponse =
    { messageId : String
    , content : String
    , sentAt : String
    }


sendMessageBodyValue : String -> JE.Value
sendMessageBodyValue content =
    JE.object
        [ ( "content", JE.string content )
        ]


sendMessageResponseDecoder : JD.Decoder SendMessageResponse
sendMessageResponseDecoder =
    JD.map3 SendMessageResponse
        (JD.field "messageId" JD.string)
        (JD.field "content" JD.string)
        (JD.field "sentAt" JD.string)


sendMessage : String -> Cmd Msg
sendMessage content =
    Http.post
        { url = "http://localhost:9000/queues/queue/messages"
        , body = Http.jsonBody <| sendMessageBodyValue content
        , expect = Http.expectJson GotSendMessageResponse sendMessageResponseDecoder
        }


type alias Model =
    { timezone : Time.Zone
    , content : String
    , sentMessageList : List SendMessageResponse
    }


init : () -> ( Model, Cmd Msg )
init _ =
    ( { content = "", sentMessageList = [], timezone = Time.utc }, Task.perform GotTimezone Time.here )



-- UPDATE


type Msg
    = GotTimezone Time.Zone
    | UpdateContent String
    | SendMessage
    | SendMessageSubscribe Time.Posix
    | GotSendMessageResponse (Result Http.Error SendMessageResponse)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    let
        { content, sentMessageList, timezone } =
            model
    in
    case msg of
        GotTimezone tz ->
            ( { model | timezone = tz }, Cmd.none )

        UpdateContent c ->
            ( { model | content = c }, Cmd.none )

        SendMessage ->
            if not <| String.isEmpty content then
                ( model, sendMessage content )

            else
                ( model, Cmd.none )

        SendMessageSubscribe posix ->
            ( model
            , sendMessage <|
                "subscribe < "
                    ++ String.fromInt (Time.toSecond timezone posix)
                    ++ ":"
                    ++ String.fromInt (Time.toMillis timezone posix)
            )

        GotSendMessageResponse result ->
            case result of
                Ok sentMessageResponse ->
                    ( { model | sentMessageList = sentMessageResponse :: sentMessageList, content = "" }, Cmd.none )

                Err _ ->
                    ( model, Cmd.none )



-- VIEW


view : Model -> Html Msg
view model =
    let
        { content, sentMessageList } =
            model
    in
    main_ [ class "ly_cont" ]
        [ input [ value content, onInput UpdateContent ] []
        , button [ onClick SendMessage ] [ text "sent message" ]
        , ul [] <|
            List.map
                (\sentMessage ->
                    li [] [ text <| sentMessage.messageId ++ " " ++ sentMessage.content ++ " " ++ sentMessage.sentAt ]
                )
                sentMessageList
        ]


subscriptions : Model -> Sub Msg
subscriptions _ =
    Time.every 500 SendMessageSubscribe
