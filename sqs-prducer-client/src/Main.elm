module Main exposing (main)

import Browser
import Html exposing (Html, button, main_, p, text)
import Html.Attributes exposing (class)
import Html.Events exposing (onClick)
import Http
import Json.Decode as JD



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


type alias User =
    { id : Int, name : String }


userDecoder : JD.Decoder User
userDecoder =
    JD.map2 User
        (JD.field "id" JD.int)
        (JD.field "name" JD.string)


getUser : Cmd Msg
getUser =
    Http.get
        { url = "http://localhost:9000/"
        , expect = Http.expectJson GotUser userDecoder
        }


type alias Model =
    { user : User }


init : () -> ( Model, Cmd Msg )
init _ =
    ( { user = { id = -1, name = "noUser" } }, getUser )



-- UPDATE


type Msg
    = GotUser (Result Http.Error User)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GotUser result ->
            case result of
                Ok user ->
                    ( { model | user = user }, Cmd.none )

                Err _ ->
                    ( model, Cmd.none )



-- VIEW


view : Model -> Html Msg
view model =
    main_ [ class "ly_cont" ]
        [ p [] [ text <| String.fromInt model.user.id ]
        , p [] [ text model.user.name ]
        ]


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none
