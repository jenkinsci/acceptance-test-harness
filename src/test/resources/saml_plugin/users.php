<?php

$config = array(

    'admin' => array(
        'core:AdminPassword',
    ),

    'example-userpass' => array(
        'exampleauth:UserPass',
        'user1:user1pass' => array(
            'uid' => array('user1'),
            'eduPersonAffiliation' => array('group1'),
            'email' => 'user1@example.com',
            'displayName' => 'User 1'
        ),
        'user2:user2pass' => array(
            'uid' => array('user2'),
            'eduPersonAffiliation' => array('group2'),
            'email' => 'user2@example.com',
            'displayName' => 'User 2'
        ),
    ),

);