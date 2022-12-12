Ispas Alexandra 344C1
# Tema 2 SPRC

Testele din Postman se ruleaza de pe local. Initial voiam sa fac ca si acestea
sa ruleze intr-un container, dar am intampinat mai multe probleme.

Pentru API-ul bazei de date am folosit pgAdmin.
Pentru conectare, se acceseaza http://localhost:5050/ cu urmatoarele credentiale:
> email: sprc@gmail.com\
> parola: sprc

Pentru a putea vizualiza datele din tabele, sunt necesari urmatorii pasi:
* Click-dreapta pe Servers -> Register -> Servers..
* Aici se adauga un nume aleatoriu
* In sectiunea 'Connection', pentru Host name/address se va folosi numele containerului\
care tine baza de date (tema2), iar parola este "sprc".

Tablelele propriu-zise se afla in urmaroare locatie in ierarhie:
> Servers/numele serverului adaugat/Databases/sprc/Schemas/Tables

Dupa ce s-au adaugat date intr-o tabela, este necesar as se dea refresh pentru a putea
fi vizualizate schimbarile (click dreapta pe Servers -> refersh).