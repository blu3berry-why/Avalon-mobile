# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.


import os
import requests

os.environ['NO_PROXY'] = '127.0.0.1'

# url = 'http://127.0.0.1:3000'
url = 'https://ktor-avalon-rest.herokuapp.com'

yes_vote = {
    'username': 'valami',
    'uservote': 'true'
}

no_vote = {
    'username': 'valami',
    'uservote': 'false'
}

users = [
    {
        "username": "testUser1",
        "password": "jelszo123"
    },
    {
        "username": "testUser2",
        "password": "jelszo123"
    },
    {
        "username": "testUser3",
        "password": "jelszo123"
    },
    {
        "username": "testUser4",
        "password": "jelszo123"
    },
    {
        "username": "testUser5",
        "password": "jelszo123"
    }
]

credentials = []

headers = {
    'Content-type': 'application/json',
    'Accept': 'application/json'
}

lobby_code = ''


def vote_yes():
    for c in credentials:
        my_headers = {'Authorization': f'Bearer {c}'}
        requests.post(url + f"/game/{lobby_code}/vote", headers=my_headers, json=yes_vote)


def vote_no():
    for c in credentials:
        my_headers = {'Authorization': f'Bearer {c}'}
        requests.post(url + f"/game/{lobby_code}/vote", headers=my_headers, json=no_vote)


def vote_adventure_yes():
    for c in credentials:
        my_headers = {'Authorization': f'Bearer {c}'}
        requests.post(url + f"/game/{lobby_code}/adventurevote", headers=my_headers, json=yes_vote)


def vote_adventure_no():
    for c in credentials:
        my_headers = {'Authorization': f'Bearer {c}'}
        requests.post(url + f"/game/{lobby_code}/adventurevote", headers=my_headers, json=no_vote)


def login():
    for u in users:
        res = requests.post(url + "/login",
                            json=u, headers=headers)
        credentials.append(res.json()["token"])


def join():
    for c in credentials:
        my_headers = {'Authorization': f'Bearer {c}'}
        requests.post(url + f"/join/{lobby_code}", headers=my_headers)


def test():
    response = requests.get(url + "/")
    print(response.text)


running = True

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    login()
    lobby_code = input("Add the lobby's code  \n >")
    join()
    while running:
        print("Type:  \n vy - for voting yes \n vn for voting no \n ay - for voting adventure yes \n "
              "an - for voting adventure no \n e - for exit")
        c = input(">")
        if c == 'vy':
            vote_yes()
        elif c == 'vn':
            vote_no()
        elif c == 'ay':
            vote_adventure_yes()
        elif c == 'an':
            vote_adventure_no()
        elif c == 'e':
            running = False
        else:
            print('wrong input try again!')

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
